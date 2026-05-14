package com.klaraapi.dto;

import com.klaraapi.entity.Category;

public record CategoryResponseDTO(Long id, String name, String description) {
    public static CategoryResponseDTO from(Category category) {
        return new CategoryResponseDTO(category.getId(), category.getName(), category.getDescription());
    }
}
