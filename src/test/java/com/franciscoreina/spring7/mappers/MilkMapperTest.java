package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = MilkMapperImpl.class)
class MilkMapperTest {

    @Autowired
    private MilkMapper mapper;

    @Test
    void toEntity_shouldCreateMilk() {
        // Arrange
        var category = Category.createCategory("Protein");

        var request = new MilkRequest(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(UUID.randomUUID())
        );

        // Act
        var milk = mapper.toEntity(request, Set.of(category));

        // Assert
        assertThat(milk).isNotNull();
        assertThat(milk.getName()).isEqualTo(request.name());
        assertThat(milk.getMilkType()).isEqualTo(request.milkType());
        assertThat(milk.getUpc()).isEqualTo(request.upc());
        assertThat(milk.getPrice()).isEqualTo(request.price());
        assertThat(milk.getStock()).isEqualTo(request.stock());
        assertThat(milk.getCategories()).contains(category);
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestIsNull() {
        // Arrange
        var category = Category.createCategory("Protein");

        // Act
        var result = mapper.toEntity(null, Set.of(category));

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldReturnNull_whenCategoriesAreNull() {
        // Arrange
        var request = new MilkRequest(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(UUID.randomUUID())
        );

        // Act
        var result = mapper.toEntity(request, null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Arrange
        var oldCategory = Category.createCategory("Old");
        var milk = Milk.createMilk(
                "Old Milk",
                MilkType.SKIMMED,
                "OLD123",
                new BigDecimal("1.50"),
                5,
                Set.of(oldCategory)
        );

        var newCategory = Category.createCategory("New");
        var request = new MilkRequest(
                "New Milk",
                MilkType.WHOLE,
                "NEW123",
                new BigDecimal("2.50"),
                10,
                Set.of(UUID.randomUUID())
        );

        // Act
        mapper.updateEntity(milk, request, Set.of(newCategory));

        // Assert
        assertThat(milk.getName()).isEqualTo(request.name());
        assertThat(milk.getMilkType()).isEqualTo(request.milkType());
        assertThat(milk.getUpc()).isEqualTo(request.upc());
        assertThat(milk.getPrice()).isEqualTo(request.price());
        assertThat(milk.getStock()).isEqualTo(request.stock());
        assertThat(milk.getCategories()).contains(newCategory);
    }

    @Test
    void updateEntity_shouldDoNothing_whenTargetIsNull() {
        // Arrange
        var newCategory = Category.createCategory("New");
        var request = new MilkRequest(
                "New Milk",
                MilkType.WHOLE,
                "NEW123",
                new BigDecimal("2.50"),
                10,
                Set.of(UUID.randomUUID())
        );

        // Act + Assert
        assertDoesNotThrow(() -> mapper.updateEntity(null, request, Set.of(newCategory)));
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestIsNull() {
        // Arrange
        var oldCategory = Category.createCategory("Old");
        var milk = Milk.createMilk(
                "Old Milk",
                MilkType.SKIMMED,
                "OLD123",
                new BigDecimal("1.50"),
                5,
                Set.of(oldCategory)
        );

        // Act + Assert
        assertDoesNotThrow(() -> mapper.updateEntity(milk, null, null));
    }

    @Test
    void patchEntity_shouldUpdateOnlyNonNullFields() {
        // Arrange
        var oldCategory = Category.createCategory("Old");
        var milk = Milk.createMilk(
                "Old Milk",
                MilkType.WHOLE,
                "OLD123",
                new BigDecimal("1.50"),
                10,
                Set.of(oldCategory)
        );

        var patch = new MilkPatchRequest(
                "Patched Milk",
                null,
                null,
                new BigDecimal("2.50"),
                null,
                null
        );

        // Act
        mapper.patchEntity(milk, patch, null);

        // Assert
        assertThat(milk.getName()).isEqualTo(patch.name());
        assertThat(milk.getMilkType()).isEqualTo(MilkType.WHOLE); // unchanged
        assertThat(milk.getUpc()).isEqualTo("OLD123"); // unchanged
        assertThat(milk.getPrice()).isEqualTo(patch.price());
        assertThat(milk.getStock()).isEqualTo(10); // unchanged
        assertThat(milk.getCategories()).contains(oldCategory); // unchanged
    }

    @Test
    void patchEntity_shouldReplaceCategories_whenNewCategoriesProvided() {
        // Arrange
        var oldCategory = Category.createCategory("Old");
        var milk = Milk.createMilk(
                "Old Milk",
                MilkType.WHOLE,
                "OLD123",
                new BigDecimal("2.50"),
                10,
                Set.of(oldCategory)
        );

        var newCategory = Category.createCategory("New");
        var request = new MilkPatchRequest(
                null,
                null,
                null,
                null,
                null,
                Set.of(UUID.randomUUID())
        );

        // Act
        mapper.patchEntity(milk, request, Set.of(newCategory));

        // Assert
        assertThat(milk.getCategories()).contains(newCategory);
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        var categoryId1 = UUID.randomUUID();
        var categoryId2 = UUID.randomUUID();
        var category1 = Category.createCategory("Protein");
        var category2 = Category.createCategory("Organic");

        ReflectionTestUtils.setField(category1, "id", categoryId1);
        ReflectionTestUtils.setField(category2, "id", categoryId2);

        var milk = Milk.createMilk(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(category1, category2)
        );

        ReflectionTestUtils.setField(milk, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(milk, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));
        ReflectionTestUtils.setField(milk, "updatedAt", Instant.parse("2026-04-01T11:00:00Z"));

        // Act
        var response = mapper.toResponse(milk);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(milk.getId());
        assertThat(response.createdAt()).isEqualTo(milk.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(milk.getUpdatedAt());
        assertThat(response.name()).isEqualTo(milk.getName());
        assertThat(response.milkType()).isEqualTo(milk.getMilkType());
        assertThat(response.upc()).isEqualTo(milk.getUpc());
        assertThat(response.price()).isEqualTo(milk.getPrice());
        assertThat(response.stock()).isEqualTo(milk.getStock());
        assertThat(response.categoryIds()).contains(categoryId1, categoryId2);
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}