package com.klaraapi.service;

import com.klaraapi.dto.CategoryRequestDTO;
import com.klaraapi.dto.CategoryResponseDTO;
import com.klaraapi.entity.Category;
import com.klaraapi.exception.BusinessException;
import com.klaraapi.exception.ResourceNotFoundException;
import com.klaraapi.repository.CategoryRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    CategoryRepository repository;

    @InjectMocks
    CategoryService service;

    @Nested
    class Create {

        @Test
        void shouldReturnDTO_whenNameIsUnique() {
            var request = new CategoryRequestDTO("Alimentação", "Supermercado");
            given(repository.existsByNameIgnoreCase("Alimentação")).willReturn(false);
            given(repository.save(any())).willAnswer(inv -> withId(inv.getArgument(0)));

            CategoryResponseDTO result = service.create(request);

            assertThat(result.id()).isEqualTo(1L);
            assertThat(result.name()).isEqualTo("Alimentação");
            assertThat(result.description()).isEqualTo("Supermercado");
        }

        @Test
        void shouldThrowBusinessException_whenNameAlreadyExists() {
            var request = new CategoryRequestDTO("Alimentação", null);
            given(repository.existsByNameIgnoreCase("Alimentação")).willReturn(true);

            assertThatThrownBy(() -> service.create(request))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Alimentação");

            then(repository).should().existsByNameIgnoreCase("Alimentação");
            then(repository).shouldHaveNoMoreInteractions();
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnAllCategoriesSortedByName() {
            var alimentacao = category(1L, "Alimentação");
            var transporte = category(2L, "Transporte");
            given(repository.findAll(any(Sort.class))).willReturn(List.of(alimentacao, transporte));

            List<CategoryResponseDTO> result = service.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.getFirst().name()).isEqualTo("Alimentação");
            assertThat(result.getLast().name()).isEqualTo("Transporte");
        }

        @Test
        void shouldReturnEmptyList_whenNoCategories() {
            given(repository.findAll(any(Sort.class))).willReturn(List.of());

            assertThat(service.findAll()).isEmpty();
        }
    }

    @Nested
    class Delete {

        @Test
        void shouldDelete_whenCategoryExists() {
            given(repository.existsById(1L)).willReturn(true);

            service.delete(1L);

            then(repository).should().deleteById(1L);
        }

        @Test
        void shouldThrowResourceNotFoundException_whenCategoryNotFound() {
            given(repository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> service.delete(99L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("99");

            then(repository).shouldHaveNoMoreInteractions();
        }
    }

    private Category withId(Category category) {
        category.setId(1L);
        return category;
    }

    private Category category(Long id, String name) {
        var c = new Category();
        c.setId(id);
        c.setName(name);
        return c;
    }
}
