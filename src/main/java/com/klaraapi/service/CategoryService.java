package com.klaraapi.service;

import com.klaraapi.dto.CategoryRequestDTO;
import com.klaraapi.dto.CategoryResponseDTO;
import com.klaraapi.entity.Category;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository repository;

    public CategoryResponseDTO create(CategoryRequestDTO request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new BusinessException("Category with name '" + request.name() + "' already exists");
        }
        var category = new Category();
        category.setName(request.name());
        category.setDescription(request.description());
        return CategoryResponseDTO.from(repository.save(category));
    }

    public List<CategoryResponseDTO> findAll() {
        return repository.findAll(Sort.by("name")).stream()
                .map(CategoryResponseDTO::from)
                .toList();
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        repository.deleteById(id);
    }
}
