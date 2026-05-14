package com.klaraapi.dto;

import com.klaraapi.enums.Recurrence;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillRequestDTO(
        @Schema(description = "Bill name", example = "Netflix")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Amount in BRL", example = "59.90")
        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @Schema(description = "Due date", example = "2026-06-01")
        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @Schema(description = "Additional details", example = "Family plan")
        String description,

        @Schema(description = "Recurrence type")
        @NotNull(message = "Recurrence is required")
        Recurrence recurrence,

        @Schema(description = "Category id (optional)", example = "1")
        Long categoryId
) {}
