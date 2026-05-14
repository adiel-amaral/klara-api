package com.klaraapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(
        @Schema(description = "Category name", example = "Food")
        @NotBlank(message = "Name is required")
        String name,

        @Schema(description = "Additional details", example = "Supermarkets, restaurants, and food delivery")
        String description
) {}
