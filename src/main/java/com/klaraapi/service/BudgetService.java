package com.klaraapi.service;

import com.klaraapi.dto.*;
import com.klaraapi.entity.Budget;
import com.klaraapi.entity.CategoryBudget;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.BudgetHealthStatus;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.BillRepository;
import com.klaraapi.repository.BudgetRepository;
import com.klaraapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final CategoryRepository categoryRepository;
    private final BillRepository billRepository;

    public BudgetResponseDTO create(BudgetRequestDTO request) {
        if (budgetRepository.existsByYearAndMonth(request.year(), request.month())) {
            throw new BusinessException(
                    "Budget already exists for " + request.year() + "/" + String.format("%02d", request.month()));
        }
        var budget = new Budget();
        budget.setYear(request.year());
        budget.setMonth(request.month());
        budget.setTotalLimit(request.totalLimit());
        populateCategoryBudgets(budget, request.categoryBudgets());
        return BudgetResponseDTO.from(budgetRepository.save(budget));
    }

    public BudgetResponseDTO update(int year, int month, BudgetRequestDTO request) {
        Budget budget = budgetRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found for " + year + "/" + String.format("%02d", month)));
        budget.setTotalLimit(request.totalLimit());
        budget.getCategoryBudgets().clear();
        populateCategoryBudgets(budget, request.categoryBudgets());
        return BudgetResponseDTO.from(budgetRepository.save(budget));
    }

    @Transactional(readOnly = true)
    public BudgetOverviewDTO getOverview(int year, int month) {
        Budget budget = budgetRepository.findByYearAndMonth(year, month)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Budget not found for " + year + "/" + String.format("%02d", month)));

        LocalDate from = LocalDate.of(year, month, 1);
        LocalDate to = from.plusMonths(1);

        BigDecimal totalSpent = billRepository.sumAmountByStatusAndPeriod(BillStatus.PAID, from, to);
        BigDecimal totalPending = billRepository.sumAmountByStatusesAndPeriod(
                List.of(BillStatus.PENDING, BillStatus.OVERDUE), from, to);
        BigDecimal totalProjected = totalSpent.add(totalPending);

        BigDecimal recurringEstimate = calculateRecurringEstimate(from);

        BigDecimal remaining = budget.getTotalLimit().subtract(totalProjected);
        BudgetHealthStatus status = healthStatus(totalProjected, budget.getTotalLimit());

        List<CategoryBudgetOverviewDTO> categories = budget.getCategoryBudgets().stream()
                .map(cb -> buildCategoryOverview(cb, from, to))
                .toList();

        return new BudgetOverviewDTO(year, month, budget.getTotalLimit(),
                totalSpent, totalPending, totalProjected, recurringEstimate, remaining, status, categories);
    }

    private void populateCategoryBudgets(Budget budget, List<CategoryBudgetRequestDTO> requests) {
        if (requests == null || requests.isEmpty()) return;
        for (var req : requests) {
            var category = categoryRepository.findById(req.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + req.categoryId()));
            var cb = new CategoryBudget();
            cb.setBudget(budget);
            cb.setCategory(category);
            cb.setLimitAmount(req.limitAmount());
            budget.getCategoryBudgets().add(cb);
        }
    }

    private CategoryBudgetOverviewDTO buildCategoryOverview(CategoryBudget cb, LocalDate from, LocalDate to) {
        Long categoryId = cb.getCategory().getId();
        BigDecimal spent = billRepository.sumAmountByStatusAndPeriodAndCategory(BillStatus.PAID, from, to, categoryId);
        BigDecimal pending = billRepository.sumAmountByStatusesAndPeriodAndCategory(
                List.of(BillStatus.PENDING, BillStatus.OVERDUE), from, to, categoryId);
        BigDecimal projected = spent.add(pending);
        BigDecimal remaining = cb.getLimitAmount().subtract(projected);
        return new CategoryBudgetOverviewDTO(
                CategoryResponseDTO.from(cb.getCategory()),
                cb.getLimitAmount(),
                spent, pending, projected, remaining,
                healthStatus(projected, cb.getLimitAmount())
        );
    }

    private BigDecimal calculateRecurringEstimate(LocalDate currentMonthStart) {
        LocalDate lookbackStart = currentMonthStart.minusMonths(12);
        var recurringBills = billRepository.findRecurringBillsBeforeMonth(lookbackStart, currentMonthStart);

        // Keep only the most recent occurrence per (name, recurrence) series
        Map<String, com.klaraapi.entity.Bill> latestPerSeries = recurringBills.stream()
                .collect(Collectors.toMap(
                        b -> b.getName() + "|" + b.getRecurrence(),
                        b -> b,
                        (b1, b2) -> b1.getDueDate().isAfter(b2.getDueDate()) ? b1 : b2
                ));

        return latestPerSeries.values().stream()
                .map(b -> b.getAmount().divide(
                        BigDecimal.valueOf(b.getRecurrence().getMonthsFactor()), 2, RoundingMode.HALF_UP))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetHealthStatus healthStatus(BigDecimal projected, BigDecimal limit) {
        if (limit.compareTo(BigDecimal.ZERO) == 0) return BudgetHealthStatus.EXCEEDED;
        BigDecimal ratio = projected.divide(limit, 4, RoundingMode.HALF_UP);
        if (ratio.compareTo(BigDecimal.ONE) > 0) return BudgetHealthStatus.EXCEEDED;
        if (ratio.compareTo(new BigDecimal("0.90")) >= 0) return BudgetHealthStatus.CRITICAL;
        if (ratio.compareTo(new BigDecimal("0.75")) >= 0) return BudgetHealthStatus.WARNING;
        return BudgetHealthStatus.ON_TRACK;
    }
}
