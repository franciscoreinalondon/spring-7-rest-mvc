package com.franciscoreina.spring7.dto.response.order;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderShipmentResponse(
        UUID id,
        Instant createdAt,
        // Business Attributes
        String trackingNumber,
        // JPA Relationships
        UUID milkOrderId
) {
}
