package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLineCreateRequest(
        @NotNull
        @Min(value = 1, message = "Quantity on hand must be greater than 0")
        Integer orderQuantity,

        @NotNull
        UUID milkId
) {
}
