package com.franciscoreina.spring7.dto.request.order;

import com.franciscoreina.spring7.domain.order.OrderLineStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record OrderLineCreateRequest(
        @NotNull
        @PositiveOrZero
        Integer orderQuantity,

        @NotNull
        @PositiveOrZero
        Integer stockAllocated,

        @Enumerated(EnumType.STRING)
        OrderLineStatus orderLineStatus
) {
}
