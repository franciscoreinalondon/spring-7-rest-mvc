package com.franciscoreina.spring7.dto.response.order;

import java.time.Instant;
import java.util.UUID;

public record OrderShipmentResponse(
        UUID id,
        Integer version,
        String trackingNumber,
        Instant createdAt,
        Instant updatedAt,
        // JPA Relationships
        UUID milkOrderId
) {
}
