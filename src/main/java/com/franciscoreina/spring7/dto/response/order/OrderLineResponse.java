package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.OrderLineStatus;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Builder
public record OrderLineResponse(
        UUID id,
        Instant createdAt,
        // Business Attributes
        Integer requestedQuantity,
        Integer assignedQuantity,
        OrderLineStatus orderLineStatus,
        BigDecimal priceAtPurchase,
        // JPA Relationships
        UUID milkOrderId,
        UUID milkId
) {
}
