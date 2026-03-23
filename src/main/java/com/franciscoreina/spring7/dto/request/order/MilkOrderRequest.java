package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record MilkOrderRequest(

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[A-Z0-9-]+$")
        String customerRef,

        // JPA Relationships

        @NotNull
        UUID customerId,

        @NotEmpty
        Set<@Valid OrderLineCreateRequest> orderLines
) {
}
