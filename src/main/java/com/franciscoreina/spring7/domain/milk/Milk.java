package com.franciscoreina.spring7.domain.milk;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import static com.franciscoreina.spring7.domain.base.DomainAssert.cannotRemoveLastElement;
import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositive;
import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositiveOrZero;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notEmpty;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milks")
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
    @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Only letters and numbers are allowed")
    @Column(nullable = false, length = 50, unique = true)
    private String upc;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock;

    // JPA Relationships

    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "milk_categories",
            joinColumns = @JoinColumn(name = "milk_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    // Factory Method

    public static Milk createMilk(String name,
                                  MilkType milkType,
                                  String upc,
                                  BigDecimal price,
                                  Integer stock,
                                  Set<Category> categories) {

        var normalizedName = normalizeName(name);
        var normalizedUpc = normalizeUpc(upc);
        validateMilkType(milkType);
        validatePrice(price);
        validateStock(stock);
        validateCategories(categories);

        Milk milk = new Milk(normalizedName, milkType, normalizedUpc, price, stock, new HashSet<>());
        categories.forEach(milk::addCategory);

        return milk;
    }

    // Business Methods (Rich Model)

    public void renameTo(String newName) {
        this.name = normalizeName(newName);
    }

    public void updateMilkType(MilkType newMilkType) {
        validateMilkType(newMilkType);
        this.milkType = newMilkType;
    }

    public void updateUpc(String newUpc) {
        this.upc = normalizeUpc(newUpc);
    }

    public void updatePrice(BigDecimal newPrice) {
        validatePrice(newPrice);
        this.price = newPrice;
    }

    public void updateStock(Integer newStock) {
        validateStock(newStock);
        this.stock = newStock;
    }

    public void decreaseStock(Integer amount) {
        isPositive(amount, "Amount must be greater than 0");
        checkStockAvailable(amount);
        this.stock -= amount;
    }

    public Set<Category> getCategories() {
        return Collections.unmodifiableSet(categories);
    }

    public void addCategory(Category category) {
        notNull(category, "Category is required");
        this.categories.add(category);
    }

    public void removeCategory(Category category) {
        cannotRemoveLastElement(categories, category, "Milk product must have at least one category");
        this.categories.remove(category);
    }

    public void replaceCategories(Set<Category> newCategories) {
        validateCategories(newCategories);
        this.categories.clear();
        newCategories.forEach(this::addCategory);
    }

    // Utilities

    private static String normalizeName(String name) {
        notBlank(name, "Name is required");
        return name.trim();
    }

    private static String normalizeUpc(String upc) {
        notBlank(upc, "UPC is required");
        var normalized = upc.trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^[A-Za-z0-9]+$")) {
            throw new IllegalArgumentException("Only letters and numbers are allowed");
        }

        return normalized;
    }

    private static void validateMilkType(MilkType milkType) {
        notNull(milkType, "MilkType is required");
    }

    private static void validatePrice(BigDecimal price) {
        notNull(price, "Price is required");
        isPositive(price, "Price must be greater than 0");

        if (price.scale() > 2) {
            throw new IllegalArgumentException("Price must have at most 2 decimal places");
        }
    }

    private static void validateStock(Integer stock) {
        notNull(stock, "Stock is required");
        isPositiveOrZero(stock, "Stock must be 0 or greater");
    }

    private static void validateCategories(Set<Category> categories) {
        notEmpty(categories, "At least one category is required");
    }

    private void checkStockAvailable(Integer amount) {
        if (this.stock < amount) {
            throw new IllegalStateException("Not enough stock available");
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
