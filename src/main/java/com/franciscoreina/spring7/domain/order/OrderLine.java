package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import com.franciscoreina.spring7.domain.milk.Milk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositive;
import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositiveOrZero;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milk_order_lines")
public class OrderLine extends BaseEntity {

    // Business Attributes

    @NotNull
    @Positive
    @Column(nullable = false)
    private Integer requestedQuantity;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer assignedQuantity;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus orderLineStatus;

    @NotNull
    @Positive
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;

    // JPA Relationships

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_order_id", nullable = false)
    private MilkOrder milkOrder;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_id", nullable = false)
    private Milk milk;

    // Factory Method

    public static OrderLine createOrderLine(Milk milk, Integer requestedQuantity) {
        validateMilk(milk);
        validateRequestedQuantity(requestedQuantity);

        var priceAtPurchase = milk.getPrice();
        validatePrice(priceAtPurchase);

        return new OrderLine(requestedQuantity,
                0,
                OrderLineStatus.NEW,
                priceAtPurchase,
                null,
                milk);
    }

    // Business Methods (Rich Model)

    void setMilkOrder(MilkOrder milkOrder) {
        this.milkOrder = milkOrder;
    }

    void updateRequestedQuantity(Integer newQuantity) {
        validateRequestedQuantity(newQuantity);

        this.requestedQuantity = newQuantity;

        // Release reserved stock if needed
        if (this.assignedQuantity > newQuantity) {
            this.assignedQuantity = newQuantity;
        }

        refreshStatus();
    }

    // Assign stock incrementally
    public void assignQuantity(Integer quantity) {
        notNull(quantity, "Quantity is required");
        isPositiveOrZero(quantity, "Quantity cannot be negative");
        validateAssignedQuantity(quantity);

        this.assignedQuantity += quantity;
        refreshStatus();
    }

    // Assign based on available stock
    public void allocateUpTo(Integer availableStock) {
        notNull(availableStock, "Available stock is required");
        isPositiveOrZero(availableStock, "Available stock cannot be negative");

        this.assignedQuantity = Math.min(this.requestedQuantity, availableStock);
        refreshStatus();
    }

    private void refreshStatus() {
        if (this.assignedQuantity == 0) {
            this.orderLineStatus = OrderLineStatus.OUT_OF_STOCK;
        } else if (this.assignedQuantity < this.requestedQuantity) {
            this.orderLineStatus = OrderLineStatus.PARTIALLY_ALLOCATED;
        } else {
            this.orderLineStatus = OrderLineStatus.FULLY_ALLOCATED;
        }
    }

    public void fullyAllocateLine() {
        this.orderLineStatus = OrderLineStatus.FULLY_ALLOCATED;
    }

    // Utilities

    private static void validateRequestedQuantity(Integer requestedQuantity) {
        notNull(requestedQuantity, "Requested quantity is required");
        isPositive(requestedQuantity, "Requested quantity must be greater than 0");
    }

    private void validateAssignedQuantity(Integer quantity) {
        if (this.assignedQuantity + quantity > this.requestedQuantity) {
            throw new IllegalStateException("Cannot assign more stock than ordered");
        }
    }

    private static void validateMilk(Milk milk) {
        notNull(milk, "Milk is required");
    }

    private static void validatePrice(BigDecimal price) {
        notNull(price, "Price is required");
        isPositive(price, "Price must be greater than 0");

        if (price.scale() > 2) {
            throw new IllegalArgumentException("Price must have at most 2 decimal places");
        }
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine that)) return false;
        return super.getId() != null && super.getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
