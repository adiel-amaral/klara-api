package com.klaraapi.dto;

import com.klaraapi.entity.Bill;
import com.klaraapi.entity.BillStatus;
import com.klaraapi.entity.Recurrence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BillResponseDTO(
        Long id,
        String name,
        BigDecimal amount,
        LocalDate dueDate,
        String description,
        Recurrence recurrence,
        BillStatus status,
        LocalDateTime createdAt
) {
    public static BillResponseDTO from(Bill bill) {
        return new BillResponseDTO(
                bill.getId(),
                bill.getName(),
                bill.getAmount(),
                bill.getDueDate(),
                bill.getDescription(),
                bill.getRecurrence(),
                bill.getStatus(),
                bill.getCreatedAt()
        );
    }
}
