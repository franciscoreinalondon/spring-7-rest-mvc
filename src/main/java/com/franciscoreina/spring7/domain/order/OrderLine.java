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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositiveOrZero;
import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositive;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milk_order_line")
public class OrderLine extends BaseEntity {

    // Business Attributes

    @NotNull
    @Positive(message = "Requested quantity must be greater than 0")
    @Column(nullable = false)
    private Integer requestedQuantity;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer assignedQuantity;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus orderLineStatus = OrderLineStatus.NEW;

    @NotNull
    @Positive(message = "Price at purchase must be greater than 0")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceAtPurchase;

    // JPA Relationships

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_order_id", nullable = false)
    private MilkOrder milkOrder;

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_id", nullable = false)
    private Milk milk;

    // Factory Method

    public static OrderLine createOrderLine(Milk milk, Integer requestedQuantity) {
        notNull(milk, "Milk is required");
        notNull(milk.getPrice(), "Price milk is required");
        notNull(requestedQuantity, "Requested quantity is required");
        isPositiveOrZero(requestedQuantity, "Requested quantity cannot be negative");

        return OrderLine.builder()
                .milk(milk)
                .requestedQuantity(requestedQuantity)
                .priceAtPurchase(milk.getPrice())
                .assignedQuantity(0)
                .orderLineStatus(OrderLineStatus.NEW)
                .build();
    }

    // Business Methods (Rich Model)

    void setMilkOrder(MilkOrder milkOrder) {
        notNull(milkOrder, "MilkOrder is required");
        this.milkOrder = milkOrder;
    }

    //  Updates the requested quantity based on customer intent.
    void updateQuantity(Integer newQuantity) {
        isPositive(newQuantity, "Quantity must be greater than 0");
        this.requestedQuantity = newQuantity;
    }

    //tbr
    // Allocates available inventory to fulfill the requested quantity.
    public void assignQuantity(Integer quantity) {
        isPositiveOrZero(quantity, "Quantity cannot be negative");
        validateAssignedQuantity(quantity, "Cannot assign more stock than ordered");

        this.assignedQuantity += quantity;

        // Si llegamos al total, podríamos cambiar el estado automáticamente
        if (this.assignedQuantity.equals(this.requestedQuantity)) {
            this.orderLineStatus = OrderLineStatus.ALLOCATED;
        }
    }

    private void validateAssignedQuantity(Integer quantity, String message) {
        if (this.assignedQuantity + quantity > this.requestedQuantity) {
            throw new IllegalStateException(message);
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
