package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.OrderLineStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderLineResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        Integer requestedQuantity,
        Integer assignedQuantity,
        OrderLineStatus orderLineStatus,
        BigDecimal priceAtPurchase,
        UUID milkOrderId,
        UUID milkId
) {
}
