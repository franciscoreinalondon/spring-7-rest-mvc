package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk_order_shipment")
public class OrderShipment extends BaseEntity {

    // Entity attributes

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String trackingNumber;

    // JPA Relationships

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "milk_order_id", nullable = false, unique = true)
    private MilkOrder milkOrder;

    // Methods

    void setMilkOrder(MilkOrder milkOrder) {
        this.milkOrder = milkOrder;
    }

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
