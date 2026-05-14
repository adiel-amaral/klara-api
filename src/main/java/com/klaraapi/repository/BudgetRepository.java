package com.klaraapi.repository;

import com.klaraapi.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    Optional<Budget> findByYearAndMonth(int year, int month);
    boolean existsByYearAndMonth(int year, int month);
}
