package com.franciscoreina.spring7.dto.response.order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MilkOrderResponse(
        UUID id,
        Integer version,
        String customerRef,
        BigDecimal paymentAmount,
        Instant createdAt,
        Instant updatedAt,
        // JPA Relationships
        UUID customerId,
        Set<UUID> orderLineIds,
        UUID orderShipmentId
) {
}
