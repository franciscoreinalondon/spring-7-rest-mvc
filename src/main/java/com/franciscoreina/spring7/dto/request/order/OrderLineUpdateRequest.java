package com.franciscoreina.spring7.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record OrderLineUpdateRequest(

        @NotNull
        @Positive
        Integer requestedQuantity
) {
}
