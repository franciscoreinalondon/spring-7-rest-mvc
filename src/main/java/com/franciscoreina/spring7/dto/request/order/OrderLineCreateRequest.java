package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.franciscoreina.spring7.domain.order.OrderLineStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLineCreateRequest(
        @NotNull
        @PositiveOrZero
        Integer orderQuantity,

        @NotNull
        UUID milkId
) {
}
