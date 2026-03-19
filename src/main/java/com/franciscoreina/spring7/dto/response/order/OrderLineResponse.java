package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.OrderLineStatus;

import java.time.Instant;
import java.util.UUID;

public record OrderLineResponse(
        UUID id,
        Integer version,
        Integer orderQuantity,
        Integer stockAllocated,
        OrderLineStatus orderLineStatus,
        Instant createdAt,
        Instant updatedAt,
        // JPA Relationships
        UUID milkOrderId,
        UUID milkId
) {
}
