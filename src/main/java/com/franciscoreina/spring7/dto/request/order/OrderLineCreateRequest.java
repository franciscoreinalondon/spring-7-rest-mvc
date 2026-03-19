package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLineCreateRequest(
        @NotNull
        @PositiveOrZero
        Integer orderQuantity,

        @NotNull
        MilkResponse milkResponse
) {
}
