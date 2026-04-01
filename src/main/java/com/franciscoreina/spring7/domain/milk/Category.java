package com.franciscoreina.spring7.domain.milk;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "categories")
public class Category extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String description;

    // Factory Method

    public static Category createCategory(String description) {
        return new Category(normalizeDescription(description));
    }

    // Business Methods (Rich Model)

    public void changeDescriptionTo(String description) {
        this.description = normalizeDescription(description);
    }

    // Utilities

    private static String normalizeDescription(String description) {
        notBlank(description, "Description is required");
        return description.trim();
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
