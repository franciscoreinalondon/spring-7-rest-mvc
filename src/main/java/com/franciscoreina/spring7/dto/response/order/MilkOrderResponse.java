package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.MilkOrderStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Builder
public record MilkOrderResponse(
        UUID id,
        Instant createdAt,
        // Business Attributes
        String customerRef,
        BigDecimal paymentAmount,
        MilkOrderStatus milkOrderStatus,
        // JPA Relationships
        UUID customerId,
        Set<UUID> orderLineIds,
        UUID orderShipmentId
) {
}
