package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
class MilkRepositoryTest {

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    void save_shouldPersistMilk_whenDataIsValid() {
        // Arrange
        var milk = milk("Whole Milk", MilkType.WHOLE, "UPC123", createCategory());

        // Act
        var saved = milkRepository.saveAndFlush(milk);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(milk.getName());
        assertThat(saved.getMilkType()).isEqualTo(milk.getMilkType());
        assertThat(saved.getUpc()).isEqualTo(milk.getUpc());
        assertThat(saved.getPrice()).isEqualTo(milk.getPrice());
        assertThat(saved.getStock()).isEqualTo(milk.getStock());
        assertThat(saved.getCategories()).hasSize(milk.getCategories().size());
    }

    @Test
    void save_shouldThrowException_whenUpcIsDuplicated() {
        // Arrange
        var category = createCategory();

        milkRepository.saveAndFlush(milk("Whole Milk", MilkType.WHOLE, "UPC123", category));

        var duplicated = milk("Skimmed Milk", MilkType.SKIMMED, "UPC123", category);

        // Act + Assert
        assertThatThrownBy(() -> milkRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------
    //      FIND
    // ---------------

    @Test
    void findByUpcIgnoreCase_shouldReturnMilk_whenExistsIgnoringCase() {
        // Arrange
        var saved = milkRepository.saveAndFlush(
                milk("Whole Milk", MilkType.WHOLE, "UPC123",  createCategory()));

        // Act
        var result = milkRepository.findByUpcIgnoreCase("upc123");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void existsByUpcIgnoreCase_shouldReturnTrue_whenExistsIgnoringCase() {
        // Arrange
        milkRepository.saveAndFlush(
                milk("Whole Milk", MilkType.WHOLE, "UPC123", createCategory()));

        // Act
        var exists = milkRepository.existsByUpcIgnoreCase("upc123");

        // Assert
        assertThat(exists).isTrue();
    }

    // ---------------
    //      SEARCH
    // ---------------

    @Test
    void findAllByNameContainingIgnoreCase_shouldReturnMatchingMilks() {
        // Arrange
        var category = createCategory();
        var milk1 = milkRepository.saveAndFlush(milk("Whole Milk", MilkType.WHOLE, "UPC111", category));
        var milk2 = milkRepository.saveAndFlush(milk("Whole Protein Milk", MilkType.HIGH_PROTEIN, "UPC222", category));

        var pageable = Pageable.ofSize(10);

        // Act
        var result = milkRepository.findAllByNameContainingIgnoreCase("whole", pageable);

        // Assert
        assertThat(result.getContent())
                .extracting(Milk::getId)
                .containsExactlyInAnyOrder(milk1.getId(), milk2.getId());
    }

    @Test
    void findAllByMilkType_shouldReturnMatchingMilks() {
        // Arrange
        var category = createCategory();
        var wholeMilk = milkRepository.saveAndFlush(milk("Whole Milk", MilkType.WHOLE, "UPC111", category));
        milkRepository.saveAndFlush(milk("Skimmed Milk", MilkType.SKIMMED, "UPC222", category));

        var pageable = Pageable.ofSize(10);

        // Act
        var result = milkRepository.findAllByMilkType(MilkType.WHOLE, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(wholeMilk.getId());
    }

    @Test
    void findAllByNameContainingIgnoreCaseAndMilkType_shouldReturnMatchingMilks() {
        // Arrange
        var category = createCategory();
        var milk = milkRepository.saveAndFlush(milk("Whole Milk", MilkType.WHOLE, "UPC111", category));
        milkRepository.saveAndFlush(milk("Whole Milk", MilkType.SKIMMED, "UPC222", category));

        var pageable = Pageable.ofSize(10);

        // Act
        var result = milkRepository.findAllByNameContainingIgnoreCaseAndMilkType("whole", MilkType.WHOLE, pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(milk.getId());
    }

    private Category createCategory() {
        return categoryRepository.saveAndFlush(Category.createCategory("Category"));
    }

    private Milk milk(String name, MilkType milkType, String upc, Category category) {
        return Milk.createMilk(
                name,
                milkType,
                upc,
                new BigDecimal("2.50"),
                10,
                Set.of(category)
        );
    }
}
