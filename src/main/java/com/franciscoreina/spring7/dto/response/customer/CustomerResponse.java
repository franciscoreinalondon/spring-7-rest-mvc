package com.franciscoreina.spring7.dto.response.customer;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CustomerResponse(
        UUID id,
        Instant createdAt,
        // Business Attributes
        String name,
        String email
) {
}
