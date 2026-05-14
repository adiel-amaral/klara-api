package com.klaraapi.dto;

import com.klaraapi.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDate;

public record UserAccountRequestDTO(
        @NotBlank(message = "O nome é obrigatório")
        String name,

        @NotBlank(message = "O e-mail é obrigatório")
        @Email(message = "Formato de e-mail inválido")
        String email,

        @NotNull(message = "A data de nascimento é obrigatória")
        LocalDate birthDate,

        @NotNull(message = "O sexo é obrigatório")
        Gender gender,

        String socialName,

        @NotBlank(message = "O telefone é obrigatório")
        @Pattern(regexp = "^\\+[1-9]\\d{6,14}$", message = "O telefone deve estar no formato internacional (ex.: +5548999999999)")
        String phone
) {
}
