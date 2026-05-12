package com.klaraapi.dto;

import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.Recurrence;

import java.time.LocalDate;

public record BillFilter(
        BillStatus status,
        Recurrence recurrence,
        LocalDate dueDateFrom,
        LocalDate dueDateTo
) {}