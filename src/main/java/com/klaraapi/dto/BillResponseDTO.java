package com.klaraapi.dto;

import com.klaraapi.entity.Bill;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.Recurrence;

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
        CategoryResponseDTO category,
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
                bill.getCategory() != null ? CategoryResponseDTO.from(bill.getCategory()) : null,
                bill.getCreatedAt()
        );
    }
}
