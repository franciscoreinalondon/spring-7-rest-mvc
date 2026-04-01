package com.franciscoreina.spring7.dto.response.order;

import java.time.Instant;
import java.util.UUID;

public record OrderShipmentResponse(
        UUID id,
        Instant createdAt,
        String trackingNumber,
        UUID milkOrderId
) {
}
