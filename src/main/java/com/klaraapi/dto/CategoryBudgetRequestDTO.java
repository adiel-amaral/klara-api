package com.klaraapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CategoryBudgetRequestDTO(
        @Schema(description = "Category id", example = "1")
        @NotNull(message = "Category id is required")
        Long categoryId,

        @Schema(description = "Spending limit for this category", example = "800.00")
        @NotNull(message = "Limit amount is required")
        @DecimalMin(value = "0.01", message = "Limit amount must be greater than zero")
        BigDecimal limitAmount
) {}
