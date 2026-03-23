package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.OrderLineStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderLineResponse(
        UUID id,
        Integer version,
        Integer requestedQuantity,
        Integer assignedQuantity,
        OrderLineStatus orderLineStatus,
        Instant createdAt,
        Instant updatedAt,
        // JPA Relationships
        UUID milkOrderId,
        UUID milkId
) {
}
