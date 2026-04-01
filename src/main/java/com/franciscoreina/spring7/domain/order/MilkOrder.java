package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import com.franciscoreina.spring7.domain.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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

import static com.franciscoreina.spring7.domain.base.DomainAssert.isPositive;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milk_orders")
public class MilkOrder extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Only capital letters, numbers and hyphens are allowed")
    @Column(nullable = false, length = 50, unique = true)
    private String customerRef;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal paymentAmount;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MilkOrderStatus milkOrderStatus;

    // JPA Relationships

    @NotNull
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @NotNull
    @OneToMany(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderLine> orderLines = new HashSet<>();

    @OneToOne(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderShipment orderShipment;

    // Factory Method

    public static MilkOrder createMilkOrder(Customer customer, String customerRef) {
        var normalizedCustomerRef = normalizeCustomerRef(customerRef);
        validateCustomer(customer);

        return new MilkOrder(normalizedCustomerRef,
                BigDecimal.ZERO,
                MilkOrderStatus.NEW,
                customer,
                new HashSet<>(),
                null);
    }

    // Business Methods (Rich Model)

    public Set<OrderLine> getOrderLines() {
        return Collections.unmodifiableSet(orderLines);
    }

    public void addOrderLine(OrderLine orderLine) {
        validateOrderLine(orderLine);
        assertOrderIsEditable();
        assertOrderHasNoShipment();
        assertOrderLineIsNotAssignedToAnotherOrder(orderLine);

        if (this.orderLines.add(orderLine)) {
            orderLine.setMilkOrder(this);
            this.paymentAmount = calculateTotalAmount();
        }
    }

    public void removeOrderLine(OrderLine orderLine) {
        validateOrderLine(orderLine);
        assertOrderIsEditable();
        assertOrderHasNoShipment();
        assertOrderLineBelongsToOrder(orderLine);

        if (this.orderLines.remove(orderLine)) {
            // Not required for Hibernate (orphanRemoval = true),
            // but keeps the object graph consistent in memory
            orderLine.setMilkOrder(null);
            this.paymentAmount = calculateTotalAmount();
        }
    }

    public void updateOrderLineQuantity(OrderLine orderLine, Integer newQuantity) {
        validateOrderLine(orderLine);
        validateQuantity(newQuantity);
        assertOrderIsEditable();
        assertOrderHasNoShipment();
        assertOrderLineBelongsToOrder(orderLine);

        orderLine.updateQuantity(newQuantity);
        this.paymentAmount = calculateTotalAmount();
    }

    public void addOrderShipment(String trackingNumber) {
        assertOrderIsEditable();
        assertOrderHasNoShipment();

        var orderShipment = OrderShipment.createOrderShipment(trackingNumber);
        orderShipment.setMilkOrder(this);
        this.orderShipment = orderShipment;
    }

    // Utilities

    private static String normalizeCustomerRef(String customerRef) {
        notBlank(customerRef, "Customer ref is required");
        var normalized = customerRef.trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("Only capital letters, numbers and hyphens are allowed");
        }

        return normalized;
    }

    private static void validateCustomer(Customer customer) {
        notNull(customer, "Customer is required");
    }

    private static void validateOrderLine(OrderLine orderLine) {
        notNull(orderLine, "OrderLine is required");
    }

    private static void validateQuantity(Integer newQuantity) {
        notNull(newQuantity, "Quantity is required");
        isPositive(newQuantity, "Quantity must be greater than 0");
    }

    private void assertOrderIsEditable() {
        if (this.milkOrderStatus != MilkOrderStatus.NEW) {
            throw new IllegalStateException("Only NEW orders can be modified");
        }
    }

    private void assertOrderLineBelongsToOrder(OrderLine orderLine) {
        if (!this.orderLines.contains(orderLine)) {
            throw new IllegalStateException("OrderLine does not belong to this order");
        }
    }

    private void assertOrderLineIsNotAssignedToAnotherOrder(OrderLine orderLine) {
        if (orderLine.getMilkOrder() != null && orderLine.getMilkOrder() != this) {
            throw new IllegalStateException("OrderLine already belongs to another order");
        }
    }

    private void assertOrderHasNoShipment() {
        if (this.orderShipment != null) {
            throw new IllegalStateException("Order already has a shipment");
        }
    }

    private BigDecimal calculateTotalAmount() {
        return orderLines.stream().map(
                        line -> line.getPriceAtPurchase()
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
