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
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.franciscoreina.spring7.domain.base.DomainAssert.cannotRemoveLastElement;
import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositive;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notEmpty;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
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
    @Positive(message = "Price must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stock;

    // JPA Relationships

    @Builder.Default
    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "milk_category",
            joinColumns = @JoinColumn(name = "milk_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Set<Category> categories = new HashSet<>();

    // Factory Method

    public static Milk createMilk(String name, MilkType milkType, String upc, BigDecimal price, Integer stock, Set<Category> categories) {
        notBlank(name, "Name is required");
        notNull(milkType, "MilkType is required");
        notBlank(upc, "UPC is required");
        notNull(price, "Price is required");
        notNull(stock, "Stock is required");
        notEmpty(categories, "At least one category is required");

        Milk milk = Milk.builder()
                .name(name.trim())
                .milkType(milkType)
                .upc(normalizeUpc(upc))
                .price(price)
                .stock(stock)
                .build();
        categories.forEach(milk::addCategory);

        return milk;
    }

    // Business Methods (Rich Model)

    public void renameTo(String newName) {
        notBlank(newName, "Name is required");
        this.name = newName.trim();
    }

    public void updateMilkType(MilkType milkType) {
        notNull(milkType, "MilkType is required");
        this.milkType = milkType;
    }

    public void updateUpc(String newUpc) {
        notBlank(newUpc, "Upc is required");
        this.upc = normalizeUpc(newUpc);
    }

    public void updatePrice(BigDecimal newPrice) {
        notNull(newPrice, "Price is required");
        isPositive(newPrice, "Price must be greater than 0");
        this.price = newPrice;
    }

    public void updateStock(Integer newStock) {
        notNull(newStock, "Stock is required");
        this.stock = newStock;
    }

    public void decreaseStock(Integer amount) {
        isPositive(amount, "Amount must be at least 1");
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

    // Utilities

    private static String normalizeUpc(String upc) {
        return upc.toUpperCase().trim();
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
