package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
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
    MilkRepository milkRepository;

    @Autowired
    private EntityManager entityManager;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    public void saveMilk_whenDataIsValid() {
        // Arrange-Act
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk());

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
        Milk milk = TestDataFactory.newMilk();
        Milk replica = TestDataFactory.newMilk(milk.getUpc());

        // Act
        milkRepository.saveAndFlush(milk);

        // Assert
        assertThatThrownBy(() -> milkRepository.saveAndFlush(replica))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
//TBF
//    @Test
//    public void saveMilk_throwException_whenNameIsNull() {
//        // Arrange
//        Milk milk = TestDataFactory.newMilk();
//        milk.setName(null);
//
//        // Act-Assert
//        assertThatThrownBy(() -> milkRepository.saveAndFlush(milk))
//                .isInstanceOf(ConstraintViolationException.class);
//    }
//TBF
//    @Test
//    public void saveMilk_throwException_whenUpcIsNull() {
//        // Arrange
//        Milk milk = TestDataFactory.newMilk();
//        milk.setUpc(null);
//
//        // Act-Assert
//        assertThatThrownBy(() -> milkRepository.saveAndFlush(milk))
//                .isInstanceOf(ConstraintViolationException.class);
//    }

    // Additional tests should be implemented to verify validation constraints
    // for the remaining required attributes

    // ---------------
    //      FIND
    // ---------------

    @Test
    public void findMilk_whenIdExists() {
        // Arrange
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk());
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
        Milk milk1 = TestDataFactory.newMilk();
        Milk milk2 = TestDataFactory.newMilk();
        Milk milk3 = TestDataFactory.newMilk();

        // Act
        milkRepository.saveAndFlush(milk1);
        milkRepository.saveAndFlush(milk2);
        milkRepository.saveAndFlush(milk3);

        // Assert
        assertThat(milkRepository.count()).isEqualTo(3);
    }
//TBF
//    @Test
//    public void findAllByName_whenExists() {
//        Pageable pageable = PageRequest.of(0, 20);
//
//        // Arrange
//        Milk milk1 = TestDataFactory.newMilk();
//        milk1.setName("Ultra-Fresh Skimmed");
//        milk1.setMilkType(MilkType.SKIMMED);
//
//        Milk milk2 = TestDataFactory.newMilk();
//        milk2.setName("Select Semi Skimmed");
//
//        Milk milk3 = TestDataFactory.newMilk();
//        milk3.setName("Natural A2");
//        milk3.setMilkType(MilkType.A2);
//
//        // Act
//        milkRepository.saveAndFlush(milk1);
//        milkRepository.saveAndFlush(milk2);
//        milkRepository.saveAndFlush(milk3);
//
//        // Assert
//        assertThat(milkRepository.findAllByNameContainingIgnoreCase("skimmed", pageable)
//                .getContent().size()).isEqualTo(2);
//    }
//TBF
//    @Test
//    public void findAllMilksByType_whenExists() {
//        // Arrange
//        Milk milk1 = TestDataFactory.newMilk(); // SEMI_SKIMMED milk type
//        Milk milk2 = TestDataFactory.newMilk(); // SEMI_SKIMMED milk type
//        Milk milk3 = TestDataFactory.newMilk();
//        milk3.setMilkType(MilkType.A2);
//
//        Pageable pageable = PageRequest.of(0, 20);
//
//        // Act
//        milkRepository.saveAndFlush(milk1);
//        milkRepository.saveAndFlush(milk2);
//        milkRepository.saveAndFlush(milk3);
//
//        // Assert
//        assertThat(milkRepository.findAllByMilkType(MilkType.A2, pageable)
//                .getContent().size()).isEqualTo(1);
//    }
//TBF
//    @Test
//    public void findAllByNameAndMilkType_whenExists() {
//        // Arrange
//        Milk milk1 = TestDataFactory.newMilk();
//        milk1.setName("Ultra-Fresh Skimmed");
//        milk1.setMilkType(MilkType.SKIMMED);
//
//        Milk milk2 = TestDataFactory.newMilk(); // SEMI_SKIMMED milk type
//        milk2.setName("Select Semi Skimmed");
//
//        Milk milk3 = TestDataFactory.newMilk();
//        milk3.setName("Natural A2");
//        milk3.setMilkType(MilkType.A2);
//
//        Pageable pageable = PageRequest.of(0, 20);
//
//        // Act
//        milkRepository.saveAndFlush(milk1);
//        milkRepository.saveAndFlush(milk2);
//        milkRepository.saveAndFlush(milk3);
//
//        // Assert
//        assertThat(milkRepository.findAllByNameContainingIgnoreCaseAndMilkType("skimmed", MilkType.SKIMMED, pageable)
//                .getContent().size()).isEqualTo(1);
//    }

    // ---------------
    //      UPDATE
    // ---------------
//TBF
//    @Test
//    public void updateMilk_whenIsModified() {
//        // Arrange
//        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk());
//        Integer oldVersion = saved.getVersion();
//
//        // Act
//        saved.setStock(500);
//        Milk updated = milkRepository.saveAndFlush(saved);
//
//        // Assert
//        assertThat(updated.getVersion()).isGreaterThan(oldVersion);
//        assertThat(updated.getStock()).isEqualTo(500);
//    }

    // ---------------
    //      DELETE
    // ---------------

    @Test
    public void deleteMilk_whenIdExists() {
        // Arrange
        Milk saved = milkRepository.saveAndFlush(TestDataFactory.newMilk());

        // Act
        milkRepository.deleteById(saved.getId());

        // Assert
        assertThat(milkRepository.existsById(saved.getId())).isFalse();
        assertThat(milkRepository.count()).isEqualTo(0);
    }
}
