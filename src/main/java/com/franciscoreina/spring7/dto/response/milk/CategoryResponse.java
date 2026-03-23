package com.franciscoreina.spring7.dto.response.milk;

import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

@Builder
public record CategoryResponse(
        UUID id,
        Instant createdAt,
        // Business Attributes
        String description
) {
}
