package com.klaraapi.dto;

import com.klaraapi.entity.Budget;

import java.math.BigDecimal;
import java.util.List;

public record BudgetResponseDTO(
        Long id,
        int year,
        int month,
        BigDecimal totalLimit,
        List<CategoryBudgetResponseDTO> categoryBudgets
) {
    public static BudgetResponseDTO from(Budget budget) {
        return new BudgetResponseDTO(
                budget.getId(),
                budget.getYear(),
                budget.getMonth(),
                budget.getTotalLimit(),
                budget.getCategoryBudgets().stream().map(CategoryBudgetResponseDTO::from).toList()
        );
    }
}
