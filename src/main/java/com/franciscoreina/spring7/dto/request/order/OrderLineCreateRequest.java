package com.franciscoreina.spring7.dto.request.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public record OrderLineCreateRequest(

        @NotNull
        @Positive
        Integer requestedQuantity,

        // JPA Relationships

        @NotNull
        UUID milkId
) {
}
