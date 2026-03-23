package com.franciscoreina.spring7.domain.milk;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk")
public class Milk extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilkType milkType;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String upc;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock;

    // JPA Relationships

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "milk_category",
            joinColumns = @JoinColumn(name = "milk_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    // Factory Method

    public static Milk createMilk(String name, MilkType milkType, String upc, BigDecimal price, Integer stock, Set<Category> initialCategories) {
        validateNotBlank(name, "Name is required");
        validateNotNull(milkType, "MilkType is required");
        validateNotBlank(upc, "UPC is required");
        validateNotNull(price, "Price is required");
        validateNotNull(stock, "Stock is required");
        validateNotEmpty(initialCategories, "At least one category is required");

        var milk = Milk.builder()
                .name(name.trim())
                .milkType(milkType)
                .upc(upc.trim())
                .price(price)
                .stock(stock)
                .build();
        initialCategories.forEach(milk::addCategory);

        return milk;
    }

    // Business Methods (Rich Model)

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public void addCategory(Category category) {
        validateNotNull(category, "Category is required");
        this.categories.add(category);
    }

    public void removeCategory(Category category) {
        if (categories.size() <= 1) {
            throw new IllegalStateException("A milk product must have at least one category");
        }
        this.categories.remove(category);
    }

    // Utilities

    private static void validateNotBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void validateNotEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Milk that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
