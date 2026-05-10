package com.klaraapi.specification;

import com.klaraapi.dto.BillFilter;
import com.klaraapi.entity.Bill;
import com.klaraapi.enums.BillStatus;
import com.klaraapi.enums.Recurrence;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class BillSpecification {

    private BillSpecification() {}

    public static Specification<Bill> withFilter(BillFilter filter) {
        return Specification
                .where(hasStatus(filter.status()))
                .and(hasRecurrence(filter.recurrence()))
                .and(dueDateFrom(filter.dueDateFrom()))
                .and(dueDateTo(filter.dueDateTo()));
    }

    private static Specification<Bill> hasStatus(BillStatus status) {
        return (root, query, cb) ->
                status == null ? null : cb.equal(root.get("status"), status);
    }

    private static Specification<Bill> hasRecurrence(Recurrence recurrence) {
        return (root, query, cb) ->
                recurrence == null ? null : cb.equal(root.get("recurrence"), recurrence);
    }

    private static Specification<Bill> dueDateFrom(LocalDate from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("dueDate"), from);
    }

    private static Specification<Bill> dueDateTo(LocalDate to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("dueDate"), to);
    }
}