package com.klaraapi.dto;

import com.klaraapi.enums.BillStatus;
import jakarta.validation.constraints.NotNull;

public record BillStatusUpdateRequestDTO(
        @NotNull(message = "Status is required")
        BillStatus status
) {}
