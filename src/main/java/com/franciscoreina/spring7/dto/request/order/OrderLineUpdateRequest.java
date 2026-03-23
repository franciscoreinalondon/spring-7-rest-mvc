package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OrderLineUpdateRequest(
        @NotNull
        @Min(value = 1, message = "Requested quantity must be greater than 0")
        Integer requestedQuantity,

        @NotNull
        UUID milkId
) {
}
