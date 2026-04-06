package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.milk.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
class CategoryRepositoryTest {
    
    @Autowired
    CategoryRepository categoryRepository;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    void save_shouldPersistCategory_whenDataIsValid() {
        // Arrange
        var category = Category.createCategory("Protein");

        // Act
        var savedCategory = categoryRepository.saveAndFlush(category);

        // Assert
        assertThat(savedCategory.getId()).isNotNull();
        assertThat(savedCategory.getDescription()).isEqualTo(category.getDescription());
    }

    @Test
    void save_shouldThrowException_whenDescriptionIsDuplicated() {
        // Arrange
        categoryRepository.saveAndFlush(Category.createCategory("Protein"));

        var duplicatedCategory = Category.createCategory("Protein");

        // Act + Assert
        assertThatThrownBy(() -> categoryRepository.saveAndFlush(duplicatedCategory))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------
    //      FIND
    // ---------------

    @Test
    void findById_shouldReturnCategory_whenCategoryExists() {
        // Arrange
        var savedCategory = categoryRepository.saveAndFlush(
                Category.createCategory("Protein")
        );

        // Act
        var result = categoryRepository.findById(savedCategory.getId());

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(savedCategory.getId());
        assertThat(result.get().getDescription()).isEqualTo(savedCategory.getDescription());
    }

    // ---------------
    //      SEARCH
    // ---------------

    @Test
    void findAllByDescriptionContainingIgnoreCase_whenMatchExists_shouldReturnCategories() {
        // Arrange
        var category1 = categoryRepository.saveAndFlush(Category.createCategory("Milk"));
        var category2 = categoryRepository.saveAndFlush(Category.createCategory("Whole Milk"));

        var pageable = Pageable.ofSize(10);

        // Act
        var result = categoryRepository.findByDescriptionContainingIgnoreCase("milk", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent())
                .extracting(Category::getDescription)
                .containsExactly(category1.getDescription(), category2.getDescription());
    }

    @Test
    void findAllByDescriptionContainingIgnoreCase_whenNoMatchExists_shouldReturnEmptyPage() {
        // Arrange
        var pageable = Pageable.ofSize(10);

        // Act
        var result = categoryRepository.findByDescriptionContainingIgnoreCase("Milk", pageable);

        // Assert
        assertThat(result.getContent()).isEmpty();
    }
}
