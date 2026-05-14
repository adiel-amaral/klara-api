package com.klaraapi.dto;

import com.klaraapi.enums.BudgetHealthStatus;

import java.math.BigDecimal;
import java.util.List;

public record BudgetOverviewDTO(
        int year,
        int month,
        BigDecimal totalLimit,
        BigDecimal totalSpent,
        BigDecimal totalPending,
        BigDecimal totalProjected,
        BigDecimal recurringEstimate,
        BigDecimal remaining,
        BudgetHealthStatus status,
        List<CategoryBudgetOverviewDTO> categories
) {}
