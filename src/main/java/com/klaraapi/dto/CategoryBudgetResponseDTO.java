package com.klaraapi.dto;

import com.klaraapi.entity.CategoryBudget;

import java.math.BigDecimal;

public record CategoryBudgetResponseDTO(Long id, CategoryResponseDTO category, BigDecimal limitAmount) {
    public static CategoryBudgetResponseDTO from(CategoryBudget cb) {
        return new CategoryBudgetResponseDTO(
                cb.getId(),
                CategoryResponseDTO.from(cb.getCategory()),
                cb.getLimitAmount()
        );
    }
}
