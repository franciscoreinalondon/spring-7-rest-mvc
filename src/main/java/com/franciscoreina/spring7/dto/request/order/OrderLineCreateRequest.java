package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OrderLineCreateRequest(

        @NotNull
        @Positive(message = "Requested quantity must be greater than 0")
        Integer requestedQuantity,

        // JPA Relationships

        @NotNull
        UUID milkId
) {
}
