package com.franciscoreina.spring7.domain.order;

import com.franciscoreina.spring7.domain.milk.Milk;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table(name = "milk_order_line")
public class OrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @Version
    private Integer version;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer orderQuantity;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false)
    private Integer stockAllocated;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderLineStatus orderLineStatus = OrderLineStatus.NEW;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderLine that)) return false;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // JPA Relationships

    @ManyToOne(optional = false)
    @JoinColumn(name = "milk_order_id", nullable = false)
    private MilkOrder milkOrder;

    void setMilkOrder(MilkOrder milkOrder) {
        this.milkOrder = milkOrder;
    }

    @ManyToOne(optional = false)
    @JoinColumn(name = "milk_id", nullable = false)
    private Milk milk;
}
