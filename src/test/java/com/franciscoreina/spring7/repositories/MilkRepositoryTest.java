package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
public class MilkRepositoryTest {

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MilkRepository milkRepository;

    @Autowired
    private EntityManager entityManager;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    public void saveMilk_whenDataIsValid() {
        // Arrange-Act
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isGreaterThanOrEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(milkRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    public void saveMilk_throwException_whenDataDuplicated() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk = TestDataFactory.newMilk(savedCategory);
        Milk replica = TestDataFactory.newMilk(milk.getUpc(), savedCategory);

        // Act
        milkRepository.saveAndFlush(milk);

        // Assert
        assertThatThrownBy(() -> milkRepository.saveAndFlush(replica))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void saveMilk_throwException_whenNameIsNull() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk = TestDataFactory.newMilk(savedCategory);
        milk.setName(null);

        // Act-Assert
        assertThatThrownBy(() -> milkRepository.saveAndFlush(milk))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void saveMilk_throwException_whenUpcIsNull() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk = TestDataFactory.newMilk(savedCategory);
        milk.setUpc(null);

        // Act-Assert
        assertThatThrownBy(() -> milkRepository.saveAndFlush(milk))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // Additional tests should be implemented to verify validation constraints
    // for the remaining required attributes

    // ---------------
    //      FIND
    // ---------------

    @Test
    public void findMilk_whenIdExists() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
        entityManager.clear();

        // Act
        Optional<Milk> found = milkRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get()).isNotSameAs(saved);

        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getVersion()).isEqualTo(saved.getVersion());
        assertThat(found.get().getName()).isEqualTo(saved.getName());
        assertThat(found.get().getMilkType()).isEqualTo(saved.getMilkType());
        assertThat(found.get().getUpc()).isEqualTo(saved.getUpc());
        assertThat(found.get().getPrice()).isEqualTo(saved.getPrice());
        assertThat(found.get().getStock()).isEqualTo(saved.getStock());
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void findMilk_returnEmpty_whenIdNotExists() {
        // Arrange-Act
        Optional<Milk> found = milkRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    public void findAllMilks_whenExists() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk1 = TestDataFactory.newMilk(savedCategory);
        Milk milk2 = TestDataFactory.newMilk(savedCategory);
        Milk milk3 = TestDataFactory.newMilk(savedCategory);

        // Act
        milkRepository.saveAndFlush(milk1);
        milkRepository.saveAndFlush(milk2);
        milkRepository.saveAndFlush(milk3);

        // Assert
        assertThat(milkRepository.count()).isEqualTo(3);
    }

    @Test
    public void findAllByName_whenExists() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk1 = TestDataFactory.newMilk(savedCategory);
        milk1.setName("Ultra-Fresh Skimmed");
        milk1.setMilkType(MilkType.SKIMMED);

        Milk milk2 = TestDataFactory.newMilk(savedCategory);
        milk2.setName("Select Semi Skimmed");

        Milk milk3 = TestDataFactory.newMilk(savedCategory);
        milk3.setName("Natural A2");
        milk3.setMilkType(MilkType.A2);

        Pageable pageable = PageRequest.of(0, 20);

        // Act
        milkRepository.saveAndFlush(milk1);
        milkRepository.saveAndFlush(milk2);
        milkRepository.saveAndFlush(milk3);

        // Assert
        assertThat(milkRepository.findAllByNameContainingIgnoreCase("skimmed", pageable)
                .getContent().size()).isEqualTo(2);
    }

    @Test
    public void findAllMilksByType_whenExists() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk milk1 = TestDataFactory.newMilk(savedCategory); // SEMI_SKIMMED milk type
        Milk milk2 = TestDataFactory.newMilk(savedCategory); // SEMI_SKIMMED milk type
        Milk milk3 = TestDataFactory.newMilk(savedCategory);
        milk3.setMilkType(MilkType.A2);

        Pageable pageable = PageRequest.of(0, 20);

        // Act
        milkRepository.saveAndFlush(milk1);
        milkRepository.saveAndFlush(milk2);
        milkRepository.saveAndFlush(milk3);

        // Assert
        assertThat(milkRepository.findAllByMilkType(MilkType.A2, pageable)
                .getContent().size()).isEqualTo(1);
    }

    @Test
    public void findAllByNameAndMilkType_whenExists() {
        // Arrange
        Category savedCategory = categoryRepository.save(TestDataFactory.newCategory());

        Milk milk1 = TestDataFactory.newMilk(savedCategory);
        milk1.setName("Ultra-Fresh Skimmed");
        milk1.setMilkType(MilkType.SKIMMED);

        Milk milk2 = TestDataFactory.newMilk(savedCategory); // SEMI_SKIMMED milk type
        milk2.setName("Select Semi Skimmed");

        Milk milk3 = TestDataFactory.newMilk(savedCategory);
        milk3.setName("Natural A2");
        milk3.setMilkType(MilkType.A2);

        Pageable pageable = PageRequest.of(0, 20);

        // Act
        milkRepository.saveAndFlush(milk1);
        milkRepository.saveAndFlush(milk2);
        milkRepository.saveAndFlush(milk3);

        // Assert
        assertThat(milkRepository.findAllByNameContainingIgnoreCaseAndMilkType("skimmed", MilkType.SKIMMED, pageable)
                .getContent().size()).isEqualTo(1);
    }

    // ---------------
    //      UPDATE
    // ---------------

    @Test
    public void updateMilk_whenIsModified() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
        Integer oldVersion = saved.getVersion();

        // Act
        saved.setStock(500);
        Milk updated = milkRepository.saveAndFlush(saved);

        // Assert
        assertThat(updated.getVersion()).isGreaterThan(oldVersion);
        assertThat(updated.getStock()).isEqualTo(500);
    }

    // ---------------
    //      DELETE
    // ---------------

    @Test
    public void deleteMilk_whenIdExists() {
        // Arrange
        Category savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));

        // Act
        milkRepository.deleteById(saved.getId());

        // Assert
        assertThat(milkRepository.existsById(saved.getId())).isFalse();
        assertThat(milkRepository.count()).isEqualTo(0);
    }
}
