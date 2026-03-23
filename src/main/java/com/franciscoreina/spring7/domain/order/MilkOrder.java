package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import com.franciscoreina.spring7.domain.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk_order")
public class MilkOrder extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Only capital letters, numbers and hyphens are allowed")
    @Column(nullable = false, length = 50, unique = true)
    private String customerRef;

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paymentAmount;

    // JPA Relationships

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Builder.Default
    @NotNull
    @OneToMany(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderLine> orderLines = new HashSet<>();

    @OneToOne(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderShipment orderShipment;

    // Factory Method

    public static MilkOrder createMilkOrder(Customer customer, String customerRef) {
        notNull(customer, "Customer is required");
        notBlank(customerRef, "CustomerRef is required");

        return MilkOrder.builder()
                .customer(customer)
                .customerRef(customerRef.trim().toUpperCase())
                .paymentAmount(BigDecimal.ZERO)
                .build();
    }

    // Business Methods (Rich Model)

    public Set<OrderLine> getOrderLines() {
        return Collections.unmodifiableSet(orderLines);
    }

    public void addOrderLine(OrderLine orderLine) {
        notNull(orderLine, "OrderLine is required");
        checkOrderIsModifiable("Order already has a shipment");

        if (this.orderLines.add(orderLine)) {
            orderLine.setMilkOrder(this);
            this.paymentAmount = getTotalAmount();
        }
    }

    public void removeOrderLine(OrderLine orderLine) {
        notNull(orderLine, "OrderLine is required");
        checkOrderIsModifiable("Order already has a shipment");

        if (this.orderLines.remove(orderLine)) {
            // orderLine.setMilkOrder(null); <- not needed, orphanRemoval = true
            this.paymentAmount = getTotalAmount();
        }
    }

    public void updateOrderLineQuantity(OrderLine orderLine, Integer newQuantity) {
        notNull(orderLine, "OrderLine is required");
        notNull(newQuantity, "New quantity is required");
        checkOrderIsModifiable("Order already has a shipment");
        checkOrderLineBelongsToOrder(orderLine, "OrderLine does not belong to this order");

        orderLine.updateQuantity(newQuantity);
        this.paymentAmount = getTotalAmount();
    }

    public void addOrderShipment(OrderShipment orderShipment) {
        notNull(orderShipment, "OrderShipment is required");
        checkOrderIsModifiable("Order already has a shipment");

        orderShipment.setMilkOrder(this);
        this.orderShipment = orderShipment;
    }

    // Utilities

    private void checkOrderIsModifiable(String message) {
        if (this.orderShipment != null) {
            throw new IllegalStateException(message);
        }
    }

    private void checkOrderLineBelongsToOrder(OrderLine orderLine, String message) {
        if (!this.orderLines.contains(orderLine)) {
            throw new IllegalStateException(message);
        }
    }

    private BigDecimal getTotalAmount() {
        return orderLines.stream()
                .map(line -> line.getPriceAtPurchase()
                        .multiply(BigDecimal.valueOf(line.getRequestedQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilkOrder that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
