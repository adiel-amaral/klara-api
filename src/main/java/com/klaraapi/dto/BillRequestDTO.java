package com.klaraapi.dto;

import com.klaraapi.enums.Recurrence;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public record BillRequestDTO(
        @NotBlank(message = "Name is required")
        String name,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
        BigDecimal amount,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        String description,

        @NotNull(message = "Recurrence is required")
        Recurrence recurrence
) {
}
