package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Category;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = CategoryMapperImpl.class)
class CategoryMapperTest {

    @Autowired
    private CategoryMapper mapper;

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        var category = Category.createCategory("Description");

        ReflectionTestUtils.setField(category, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(category, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));

        // Act
        var response = mapper.toResponse(category);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(category.getId());
        assertThat(response.createdAt()).isEqualTo(category.getCreatedAt());
        assertThat(response.description()).isEqualTo(category.getDescription());
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}