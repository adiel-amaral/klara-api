package com.klaraapi.repository;

import com.klaraapi.entity.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
    List<UserAccount> findByActiveTrue();
    Optional<UserAccount> findByIdAndActiveTrue(Long id);
}
