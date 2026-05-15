package com.klaraapi.dto;

import com.klaraapi.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserAccountRequestDTO(

        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,

        @NotNull(message = "Birth date is required")
        LocalDate birthDate,

        @NotNull(message = "Gender is required")
        Gender gender,

        String socialName,

        @NotBlank(message = "Phone number is required")
        @Pattern(
                regexp = "^\\+[1-9]\\d{6,14}$",
                message = "Phone number must be in international format (e.g., +5548999999999)"
        )
        String phone

) {
}
