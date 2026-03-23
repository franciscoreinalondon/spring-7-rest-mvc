package com.franciscoreina.spring7.domain.milk;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "category")
public class Category extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String description;

    // Factory Method

    public static Category createCategory(String description) {
        validateNotBlank(description, "Description is required");

        return Category.builder()
                .description(description.trim())
                .build();
    }

    // Utilities

    private static void validateNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
