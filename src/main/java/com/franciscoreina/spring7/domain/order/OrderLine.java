package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import com.franciscoreina.spring7.domain.milk.Milk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk_order_line")
public class OrderLine extends BaseEntity {

    // Business Attributes

    @NotNull
    @Min(value = 1, message = "Requested quantity must be greater than 0")
    @Column(nullable = false)
    private Integer requestedQuantity;

    @NotNull
    @PositiveOrZero()
    @Column(nullable = false)
    private Integer assignedQuantity;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus orderLineStatus = OrderLineStatus.NEW;

    @NotNull
    @DecimalMin("0.00")
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

    public static OrderLine createOrderLine(Milk milk, Integer quantity) {
        validateNotNull(milk, "Milk is required");
        validateNotNull(quantity, "Quantity is required");

        return OrderLine.builder()
                .milk(milk)
                .requestedQuantity(quantity)
                .priceAtPurchase(milk.getPrice())
                .assignedQuantity(0)
                .orderLineStatus(OrderLineStatus.NEW)
                .build();
    }

    // Business Methods (Rich Model)

    void setMilkOrder(MilkOrder milkOrder) {
        validateNotNull(milkOrder, "MilkOrder is required");
        this.milkOrder = milkOrder;
    }

    void updateQuantity(Integer newQuantity) {
        if (newQuantity == null || newQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1");
        }
        this.requestedQuantity = newQuantity;
    }

    public void assignQuantity(Integer quantity) {//tbr
        if (quantity == null || quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
        if (this.assignedQuantity + quantity > this.requestedQuantity) {
            throw new IllegalStateException("Cannot assign more stock than ordered");
        }
        this.assignedQuantity += quantity;

        // Si llegamos al total, podríamos cambiar el estado automáticamente
        if (this.assignedQuantity.equals(this.requestedQuantity)) {
            this.orderLineStatus = OrderLineStatus.FULFILLED;
        }
    }

    // Utilities

    private static void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
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
