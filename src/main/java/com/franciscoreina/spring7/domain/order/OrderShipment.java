package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.franciscoreina.spring7.domain.base.DomainAssert.notBlank;
import static com.franciscoreina.spring7.domain.base.DomainAssert.notNull;

@Builder(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // For Hibernate
@AllArgsConstructor(access = AccessLevel.PRIVATE) // For Builder
@Getter
@Setter(AccessLevel.NONE) // Defensive programming
@Entity
@Table(name = "milk_order_shipment")
public class OrderShipment extends BaseEntity {

    // Business Attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String trackingNumber;

    // JPA Relationships

    @NotNull
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_order_id", nullable = false, unique = true)
    private MilkOrder milkOrder;

    // Factory Method

    public static OrderShipment createOrderShipment(String trackingNumber, MilkOrder milkOrder) {
        notBlank(trackingNumber, "Tracking number is required");
        notNull(milkOrder, "MilkOrder is required");

        return OrderShipment.builder()
                .trackingNumber(trackingNumber.trim())
                .milkOrder(milkOrder)
                .build();
    }

    // Business Methods (Rich Model)

    public void updateTrackingNumber(String trackingNumber) {
        notBlank(trackingNumber, "Tracking number is required");
        this.trackingNumber = trackingNumber.trim();
    }

    void setMilkOrder(MilkOrder milkOrder) {
        notNull(milkOrder, "MilkOrder is required");
        this.milkOrder = milkOrder;
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
