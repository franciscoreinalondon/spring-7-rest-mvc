package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.order.MilkOrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MilkOrderResponse(
        UUID id,
        Instant createdAt,
        Instant updatedAt,
        String customerRef,
        BigDecimal paymentAmount,
        MilkOrderStatus milkOrderStatus,
        UUID customerId,
        Set<OrderLineResponse> orderLines,
        UUID orderShipmentId // nullable
) {
}
