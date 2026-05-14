package com.klaraapi.dto;

import com.klaraapi.entity.UserAccount;
import com.klaraapi.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserAccountResponseDTO(
        Long id,
        String name,
        String email,
        LocalDate birthDate,
        Gender gender,
        String socialName,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserAccountResponseDTO from(UserAccount userAccount) {
        return new UserAccountResponseDTO(
                userAccount.getId(),
                userAccount.getName(),
                userAccount.getEmail(),
                userAccount.getBirthDate(),
                userAccount.getGender(),
                userAccount.getSocialName(),
                userAccount.getPhone(),
                userAccount.isActive(),
                userAccount.getCreatedAt(),
                userAccount.getUpdatedAt()
        );
    }
}
