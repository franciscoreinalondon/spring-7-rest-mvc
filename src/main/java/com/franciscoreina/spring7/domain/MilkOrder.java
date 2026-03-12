package com.franciscoreina.spring7.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
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

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, length = 50, unique = true)
    private String customerRef;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // JPA Relationships

    @ManyToOne(optional = false)
    private Customer customer;

    @Builder.Default
    @OneToMany(mappedBy = "milkOrder")
    private Set<MilkOrderLine> milkOrderLines = new HashSet<>();

    public void addMilkOrderLine(MilkOrderLine orderLine) {
        milkOrderLines.add(orderLine);
        orderLine.setMilkOrder(this);
    }

    @OneToOne(mappedBy = "milkOrder", cascade = CascadeType.PERSIST)
    private MilkOrderShipment milkOrderShipment;

    public void addMilkOrderShipment(MilkOrderShipment orderShipment) {
        this.milkOrderShipment = orderShipment;
        orderShipment.setMilkOrder(this);
    }

}
