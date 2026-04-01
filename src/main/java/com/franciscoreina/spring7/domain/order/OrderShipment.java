package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Locale;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Factory
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milk_order_shipments")
public class OrderShipment extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[A-Z0-9-]+$", message = "Only capital letters, numbers and hyphens are allowed")
    @Column(nullable = false, length = 50, unique = true)
    private String trackingNumber;

    // JPA Relationships

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_order_id", nullable = false, unique = true)
    private MilkOrder milkOrder;

    // Factory Method

    static OrderShipment createOrderShipment(String trackingNumber) {
        return new OrderShipment(normalizeTrackingNumber(trackingNumber), null);
    }

    // Business Methods (Rich Model)

    public void updateTrackingNumber(String trackingNumber) {
        this.trackingNumber = normalizeTrackingNumber(trackingNumber);
    }

    void setMilkOrder(MilkOrder milkOrder) {
        validateMilkOrder(milkOrder);
        this.milkOrder = milkOrder;
    }

    // Utilities

    private static String normalizeTrackingNumber(String trackingNumber) {
        notBlank(trackingNumber, "Tracking number is required");
        var normalized = trackingNumber.trim().toUpperCase(Locale.ROOT);

        if (!normalized.matches("^[A-Z0-9-]+$")) {
            throw new IllegalArgumentException("Only capital letters, numbers and hyphens are allowed");
        }

        return normalized;
    }

    private static void validateMilkOrder(MilkOrder milkOrder) {
        notNull(milkOrder, "MilkOrder is required");
    }

    // Equals / HashCode

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderShipment that)) return false;
        return getId() != null && getId().equals(that.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
