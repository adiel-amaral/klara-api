package com.klaraapi.dto;

import com.klaraapi.entity.UserProfile;
import com.klaraapi.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        LocalDate birthDate,
        Gender gender,
        String socialName,
        String phone,
        LocalDateTime createdAt
) {
    public static UserResponseDTO from(UserProfile userProfile) {
        return new UserResponseDTO(
                userProfile.getId(),
                userProfile.getName(),
                userProfile.getEmail(),
                userProfile.getBirthDate(),
                userProfile.getGender(),
                userProfile.getSocialName(),
                userProfile.getPhone(),
                userProfile.getCreatedAt()
        );
    }
}
