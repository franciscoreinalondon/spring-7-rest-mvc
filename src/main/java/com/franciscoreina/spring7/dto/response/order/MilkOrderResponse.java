package com.franciscoreina.spring7.dto.response.order;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.domain.order.OrderShipment;

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
        Set<OrderLine> orderLines,
        UUID orderShipmentId
) {
}
