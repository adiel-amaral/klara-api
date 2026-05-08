package com.klaraapi.service;

import com.klaraapi.dto.BillRequestDTO;
import com.klaraapi.dto.BillResponseDTO;
import com.klaraapi.entity.Bill;
import com.klaraapi.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository repository;

    public BillResponseDTO create(BillRequestDTO request) {
        var bill = new Bill();
        bill.setName(request.name());
        bill.setAmount(request.amount());
        bill.setDueDate(request.dueDate());
        bill.setDescription(request.description());
        bill.setRecurrence(request.recurrence());
        return BillResponseDTO.from(repository.save(bill));
    }
}
