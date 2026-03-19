package com.franciscoreina.spring7.dto.request.order;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OrderShipmentCreateRequest(
        @NotBlank
        @Size(max = 50)
        String trackingNumber
) {
}
