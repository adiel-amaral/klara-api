package com.klaraapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record BudgetRequestDTO(
        @Schema(description = "Year", example = "2026")
        @NotNull(message = "Year is required")
        @Min(value = 1, message = "Year must be positive")
        Integer year,

        @Schema(description = "Month (1-12)", example = "5")
        @NotNull(message = "Month is required")
        @Min(value = 1, message = "Month must be between 1 and 12")
        @Max(value = 12, message = "Month must be between 1 and 12")
        Integer month,

        @Schema(description = "Total monthly spending limit", example = "5000.00")
        @NotNull(message = "Total limit is required")
        @DecimalMin(value = "0.01", message = "Total limit must be greater than zero")
        BigDecimal totalLimit,

        @Schema(description = "Per-category limits (optional)")
        @Valid
        List<CategoryBudgetRequestDTO> categoryBudgets
) {}
