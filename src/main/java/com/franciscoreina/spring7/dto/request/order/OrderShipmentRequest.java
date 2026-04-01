package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record OrderShipmentRequest(

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "Only capital letters, numbers and hyphens are allowed")
        String trackingNumber
) {
}
