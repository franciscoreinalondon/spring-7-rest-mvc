package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import com.franciscoreina.spring7.mappers.CategoryMapper;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import org.instancio.Instancio;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    CategoryRepository categoryRepository;
    @Mock
    CategoryMapper categoryMapper;
    @InjectMocks
    CategoryServiceImpl categoryService;

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnAllCategories_whenNoFiltersAreProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var category = Category.createCategory("Milk");
            var response = Instancio.create(CategoryResponse.class);
            var page = new PageImpl<>(List.of(category), pageable, 1);

            given(categoryRepository.findAll(pageable)).willReturn(page);
            given(categoryMapper.toResponse(category)).willReturn(response);

            // Act
            var result = categoryService.search(null, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().description()).isEqualTo(response.description());

            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void search_shouldSearchByDescription_whenDescriptionIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var category = Category.createCategory("Milk");
            var response = Instancio.create(CategoryResponse.class);
            var page = new PageImpl<>(List.of(category), pageable, 1);

            given(categoryRepository.findByDescriptionContainingIgnoreCase(category.getDescription(), pageable)).willReturn(page);
            given(categoryMapper.toResponse(category)).willReturn(response);

            // Act
            var result = categoryService.search(" Milk ", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().description()).isEqualTo(response.description());

            verify(categoryRepository).findByDescriptionContainingIgnoreCase(category.getDescription(), pageable);
        }

        @Test
        void search_shouldTreatBlankFilterAsNull() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<Category>(List.of(), pageable, 0);

            given(categoryRepository.findAll(any(Pageable.class))).willReturn(page);

            // Act
            var result = categoryService.search(" ", pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(categoryRepository).findAll(pageable);
        }

        @Test
        void search_shouldReturnNoResults_whenEmptyPage() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<Category>(List.of(), pageable, 0);

            given(categoryRepository.findByDescriptionContainingIgnoreCase("Milk", pageable)).willReturn(page);

            // Act
            var result = categoryService.search("Milk", pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(categoryRepository).findByDescriptionContainingIgnoreCase("Milk", pageable);
        }
    }
}