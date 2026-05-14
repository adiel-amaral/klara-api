package com.klaraapi.repository;

import com.klaraapi.entity.Bill;
import com.klaraapi.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long>, JpaSpecificationExecutor<Bill> {

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.status = :status AND b.dueDate >= :from AND b.dueDate < :to")
    BigDecimal sumAmountByStatusAndPeriod(@Param("status") BillStatus status, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.status IN :statuses AND b.dueDate >= :from AND b.dueDate < :to")
    BigDecimal sumAmountByStatusesAndPeriod(@Param("statuses") List<BillStatus> statuses, @Param("from") LocalDate from, @Param("to") LocalDate to);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.status = :status AND b.dueDate >= :from AND b.dueDate < :to AND b.category.id = :categoryId")
    BigDecimal sumAmountByStatusAndPeriodAndCategory(@Param("status") BillStatus status, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("categoryId") Long categoryId);

    @Query("SELECT COALESCE(SUM(b.amount), 0) FROM Bill b WHERE b.status IN :statuses AND b.dueDate >= :from AND b.dueDate < :to AND b.category.id = :categoryId")
    BigDecimal sumAmountByStatusesAndPeriodAndCategory(@Param("statuses") List<BillStatus> statuses, @Param("from") LocalDate from, @Param("to") LocalDate to, @Param("categoryId") Long categoryId);

    @Query("SELECT b FROM Bill b WHERE b.recurrence <> com.klaraapi.enums.Recurrence.ONCE AND b.dueDate >= :start AND b.dueDate < :monthStart AND b.status <> com.klaraapi.enums.BillStatus.CANCELLED")
    List<Bill> findRecurringBillsBeforeMonth(@Param("start") LocalDate start, @Param("monthStart") LocalDate monthStart);
}
