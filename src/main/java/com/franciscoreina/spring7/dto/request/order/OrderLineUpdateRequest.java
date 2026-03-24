package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OrderLineUpdateRequest(

        @NotNull
        @Positive(message = "Requested quantity must be greater than 0")
        Integer requestedQuantity
) {
}
