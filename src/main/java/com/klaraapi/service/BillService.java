package com.klaraapi.service;

import com.klaraapi.dto.*;
import com.klaraapi.entity.Bill;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.BillRepository;
import com.klaraapi.specification.BillSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class BillService {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("dueDate", "amount", "createdAt", "name");

    private static final Map<BillStatus, Set<BillStatus>> VALID_TRANSITIONS = Map.of(
            BillStatus.PENDING, Set.of(BillStatus.PAID, BillStatus.CANCELLED),
            BillStatus.OVERDUE, Set.of(BillStatus.PAID, BillStatus.CANCELLED),
            BillStatus.PAID, Set.of(),
            BillStatus.CANCELLED, Set.of()
    );

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

    public BillResponseDTO update(Long id, BillUpdateRequestDTO request) {
        Bill bill = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        bill.setName(request.name());
        bill.setAmount(request.amount());
        bill.setDueDate(request.dueDate());
        bill.setDescription(request.description());
        bill.setRecurrence(request.recurrence());
        return BillResponseDTO.from(repository.save(bill));
    }

    public BillResponseDTO updateStatus(Long id, BillStatusUpdateRequestDTO request) {
        Bill bill = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill not found with id: " + id));
        Set<BillStatus> allowed = VALID_TRANSITIONS.getOrDefault(bill.getStatus(), Set.of());
        if (!allowed.contains(request.status())) {
            throw new BusinessException(
                    "Cannot transition from " + bill.getStatus() + " to " + request.status()
            );
        }
        bill.setStatus(request.status());
        return BillResponseDTO.from(repository.save(bill));
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Bill not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public Page<BillResponseDTO> findAll(BillFilter filter, int page, int size, String sort, String direction) {
        if (!ALLOWED_SORT_FIELDS.contains(sort)) {
            throw new BusinessException("Invalid sort field '" + sort + "'. Allowed: dueDate, amount, createdAt, name");
        }
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by(sortDirection, sort));
        return repository.findAll(BillSpecification.withFilter(filter), pageable).map(BillResponseDTO::from);
    }
}
