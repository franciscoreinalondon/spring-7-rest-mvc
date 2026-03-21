package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.customer.Customer;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk_order")
public class MilkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false)
    private UUID id;

    @Version
    private Integer version;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    // Entity attributes

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

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Builder.Default
    @OneToMany(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<OrderLine> orderLines = new HashSet<>();

    @OneToOne(mappedBy = "milkOrder", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private OrderShipment orderShipment;

    // Methods

    public static MilkOrder createMilkOrder(Customer customer, String customerRef) {
        if (customer == null) throw new IllegalArgumentException("Customer cannot be null");
        if (customerRef == null || customerRef.isBlank())
            throw new IllegalArgumentException("Customer reference is required");

        return MilkOrder.builder()
                .customer(customer)
                .customerRef(customerRef)
                .paymentAmount(BigDecimal.ZERO)
                .build();
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Set<OrderLine> getOrderLines() {
        return Collections.unmodifiableSet(orderLines);
    }

    public void addOrderLine(OrderLine orderLine) {
        checkOrderIsModifiable("Cannot add lines to a shipped order");

        if (this.orderLines.add(orderLine)) {
            orderLine.setMilkOrder(this);
            this.paymentAmount = getTotalAmount();
        }
    }

    public void removeOrderLine(OrderLine orderLine) {
        checkOrderIsModifiable("Cannot remove lines to a shipped order");

        if (this.orderLines.remove(orderLine)) {
            orderLine.setMilkOrder(null);
            this.paymentAmount = getTotalAmount();
        }
    }

    private void checkOrderIsModifiable(String message) {
        if (this.orderShipment != null) {
            throw new IllegalStateException(message);
        }
    }

    public BigDecimal getTotalAmount() {
        return orderLines.stream()
                .map(line -> line.getPriceAtPurchase().multiply(BigDecimal.valueOf(line.getOrderQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addOrderShipment(OrderShipment orderShipment) {
        checkOrderIsModifiable("Order already has a shipment");
        if (orderShipment == null) {
            throw new IllegalArgumentException("Shipment cannot be null");
        }

        this.orderShipment = orderShipment;
        orderShipment.setMilkOrder(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MilkOrder that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
