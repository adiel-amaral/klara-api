package com.klaraapi.service;

import com.klaraapi.dto.*;
import com.klaraapi.entity.Bill;
import com.klaraapi.entity.Budget;
import com.klaraapi.entity.Category;
import com.klaraapi.entity.CategoryBudget;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.BudgetHealthStatus;
import com.klaraapi.enums.Recurrence;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.BillRepository;
import com.klaraapi.repository.BudgetRepository;
import com.klaraapi.repository.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    BudgetRepository budgetRepository;

    @Mock
    CategoryRepository categoryRepository;

    @Mock
    BillRepository billRepository;

    @InjectMocks
    BudgetService budgetService;

    @Nested
    class Create {

        @Test
        void shouldReturnDTO_whenBudgetDoesNotExistYet() {
            given(budgetRepository.existsByYearAndMonth(2026, 5)).willReturn(false);
            given(budgetRepository.save(any())).willAnswer(inv -> withId(inv.getArgument(0)));

            var request = new BudgetRequestDTO(2026, 5, new BigDecimal("3000.00"), null);
            var result = budgetService.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.year()).isEqualTo(2026);
            assertThat(result.month()).isEqualTo(5);
            assertThat(result.totalLimit()).isEqualByComparingTo("3000.00");
            assertThat(result.categoryBudgets()).isEmpty();
        }

        @Test
        void shouldSaveCategoryBudgets_whenProvided() {
            given(budgetRepository.existsByYearAndMonth(2026, 5)).willReturn(false);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category()));
            given(budgetRepository.save(any())).willAnswer(inv -> withId(inv.getArgument(0)));

            var categoryBudgets = List.of(new CategoryBudgetRequestDTO(1L, new BigDecimal("800.00")));
            var result = budgetService.create(new BudgetRequestDTO(2026, 5, new BigDecimal("3000.00"), categoryBudgets));

            assertThat(result.categoryBudgets()).hasSize(1);
            assertThat(result.categoryBudgets().getFirst().limitAmount()).isEqualByComparingTo("800.00");
        }

        @Test
        void shouldThrowBusinessException_whenBudgetAlreadyExists() {
            given(budgetRepository.existsByYearAndMonth(2026, 5)).willReturn(true);

            var request = new BudgetRequestDTO(2026, 5, new BigDecimal("3000.00"), null);
            assertThatThrownBy(() -> budgetService.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("2026");
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryNotFound() {
            given(budgetRepository.existsByYearAndMonth(2026, 5)).willReturn(false);
            given(categoryRepository.findById(99L)).willReturn(Optional.empty());

            var categoryBudgets = List.of(new CategoryBudgetRequestDTO(99L, new BigDecimal("800.00")));
            var request = new BudgetRequestDTO(2026, 5, new BigDecimal("3000.00"), categoryBudgets);
            assertThatThrownBy(() -> budgetService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    class Update {

        @Test
        void shouldReplaceLimits_whenBudgetExists() {
            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.of(budget(new BigDecimal("3000.00"))));
            given(budgetRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

            var result = budgetService.update(2026, 5, new BudgetRequestDTO(2026, 5, new BigDecimal("5000.00"), null));

            assertThat(result.totalLimit()).isEqualByComparingTo("5000.00");
            assertThat(result.categoryBudgets()).isEmpty();
        }

        @Test
        void shouldThrowResourceNotFoundException_whenBudgetNotFound() {
            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.empty());

            var request = new BudgetRequestDTO(2026, 5, new BigDecimal("5000.00"), null);
            assertThatThrownBy(() -> budgetService.update(2026, 5, request))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    class GetOverview {

        private final LocalDate from = LocalDate.of(2026, 5, 1);
        private final LocalDate to = LocalDate.of(2026, 6, 1);

        @Test
        void shouldReturnON_TRACK_whenSpendingIsBelow75Percent() {
            mockOverviewDependencies(new BigDecimal("3000.00"), new BigDecimal("1000.00"), new BigDecimal("500.00"));

            var result = budgetService.getOverview(2026, 5);

            assertThat(result.totalSpent()).isEqualByComparingTo("1000.00");
            assertThat(result.totalPending()).isEqualByComparingTo("500.00");
            assertThat(result.totalProjected()).isEqualByComparingTo("1500.00");
            assertThat(result.remaining()).isEqualByComparingTo("1500.00");
            assertThat(result.status()).isEqualTo(BudgetHealthStatus.ON_TRACK);
        }

        @Test
        void shouldReturnWARNING_whenSpendingIsBetween75And90Percent() {
            mockOverviewDependencies(new BigDecimal("3000.00"), new BigDecimal("2000.00"), new BigDecimal("400.00"));

            assertThat(budgetService.getOverview(2026, 5).status()).isEqualTo(BudgetHealthStatus.WARNING);
        }

        @Test
        void shouldReturnCRITICAL_whenSpendingIsBetween90And100Percent() {
            mockOverviewDependencies(new BigDecimal("3000.00"), new BigDecimal("2500.00"), new BigDecimal("400.00"));

            assertThat(budgetService.getOverview(2026, 5).status()).isEqualTo(BudgetHealthStatus.CRITICAL);
        }

        @Test
        void shouldReturnEXCEEDED_whenSpendingIsOver100Percent() {
            mockOverviewDependencies(new BigDecimal("3000.00"), new BigDecimal("2800.00"), new BigDecimal("500.00"));

            assertThat(budgetService.getOverview(2026, 5).status()).isEqualTo(BudgetHealthStatus.EXCEEDED);
        }

        @Test
        void shouldIncludeRecurringEstimate_whenRecurringBillsExistInLookback() {
            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.of(budget(new BigDecimal("3000.00"))));
            given(billRepository.sumAmountByStatusAndPeriod(BillStatus.PAID, from, to)).willReturn(BigDecimal.ZERO);
            given(billRepository.sumAmountByStatusesAndPeriod(any(), eq(from), eq(to))).willReturn(BigDecimal.ZERO);

            var monthlyBill = recurringBill("Netflix", new BigDecimal("59.90"), Recurrence.MONTHLY);
            var quarterlyBill = recurringBill("Seguro", new BigDecimal("300.00"), Recurrence.QUARTERLY);
            given(billRepository.findRecurringBillsBeforeMonth(any(), eq(from))).willReturn(List.of(monthlyBill, quarterlyBill));

            var result = budgetService.getOverview(2026, 5);

            // MONTHLY: 59.90 / 1 = 59.90 | QUARTERLY: 300.00 / 3 = 100.00 → total = 159.90
            assertThat(result.recurringEstimate()).isEqualByComparingTo("159.90");
        }

        @Test
        void shouldReturnZeroRecurringEstimate_whenNoRecurringBillsExist() {
            mockOverviewDependencies(new BigDecimal("3000.00"), BigDecimal.ZERO, BigDecimal.ZERO);

            assertThat(budgetService.getOverview(2026, 5).recurringEstimate()).isEqualByComparingTo("0.00");
        }

        @Test
        void shouldBreakdownByCategory_whenCategoryBudgetsExist() {
            var category = category();
            var budget = budget(new BigDecimal("3000.00"));
            budget.getCategoryBudgets().add(categoryBudget(budget, category, new BigDecimal("800.00")));

            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.of(budget));
            given(billRepository.sumAmountByStatusAndPeriod(BillStatus.PAID, from, to)).willReturn(BigDecimal.ZERO);
            given(billRepository.sumAmountByStatusesAndPeriod(any(), eq(from), eq(to))).willReturn(BigDecimal.ZERO);
            given(billRepository.findRecurringBillsBeforeMonth(any(), eq(from))).willReturn(List.of());
            given(billRepository.sumAmountByStatusAndPeriodAndCategory(BillStatus.PAID, from, to, 1L))
                    .willReturn(new BigDecimal("600.00"));
            given(billRepository.sumAmountByStatusesAndPeriodAndCategory(any(), eq(from), eq(to), eq(1L)))
                    .willReturn(new BigDecimal("100.00"));

            var result = budgetService.getOverview(2026, 5);

            assertThat(result.categories()).hasSize(1);
            var categoryOverview = result.categories().getFirst();
            assertThat(categoryOverview.category().name()).isEqualTo("Alimentação");
            assertThat(categoryOverview.spent()).isEqualByComparingTo("600.00");
            assertThat(categoryOverview.pending()).isEqualByComparingTo("100.00");
            assertThat(categoryOverview.projected()).isEqualByComparingTo("700.00");
            assertThat(categoryOverview.remaining()).isEqualByComparingTo("100.00");
            assertThat(categoryOverview.status()).isEqualTo(BudgetHealthStatus.WARNING);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenBudgetNotFound() {
            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.empty());

            assertThatThrownBy(() -> budgetService.getOverview(2026, 5))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        private void mockOverviewDependencies(BigDecimal limit, BigDecimal spent, BigDecimal pending) {
            given(budgetRepository.findByYearAndMonth(2026, 5)).willReturn(Optional.of(budget(limit)));
            given(billRepository.sumAmountByStatusAndPeriod(BillStatus.PAID, from, to)).willReturn(spent);
            given(billRepository.sumAmountByStatusesAndPeriod(any(), eq(from), eq(to))).willReturn(pending);
            given(billRepository.findRecurringBillsBeforeMonth(any(), eq(from))).willReturn(List.of());
        }
    }

    private Budget withId(Budget budget) {
        budget.setId(1L);
        return budget;
    }

    private Budget budget(BigDecimal totalLimit) {
        var budget = new Budget();
        budget.setId(1L);
        budget.setYear(2026);
        budget.setMonth(5);
        budget.setTotalLimit(totalLimit);
        return budget;
    }

    private Category category() {
        var c = new Category();
        c.setId(1L);
        c.setName("Alimentação");
        return c;
    }

    private CategoryBudget categoryBudget(Budget budget, Category category, BigDecimal limit) {
        var cb = new CategoryBudget();
        cb.setBudget(budget);
        cb.setCategory(category);
        cb.setLimitAmount(limit);
        return cb;
    }

    private Bill recurringBill(String name, BigDecimal amount, Recurrence recurrence) {
        var bill = new Bill();
        bill.setName(name);
        bill.setAmount(amount);
        bill.setDueDate(LocalDate.of(2026, 4, 10));
        bill.setRecurrence(recurrence);
        bill.setStatus(BillStatus.PAID);
        return bill;
    }
}
