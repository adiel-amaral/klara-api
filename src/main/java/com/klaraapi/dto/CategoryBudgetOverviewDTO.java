package com.klaraapi.dto;

import com.klaraapi.enums.BudgetHealthStatus;

import java.math.BigDecimal;

public record CategoryBudgetOverviewDTO(
        CategoryResponseDTO category,
        BigDecimal limit,
        BigDecimal spent,
        BigDecimal pending,
        BigDecimal projected,
        BigDecimal remaining,
        BudgetHealthStatus status
) {}
