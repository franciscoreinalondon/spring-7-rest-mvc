package com.franciscoreina.spring7.dto.response.milk;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponse(
        UUID id,
        Instant createdAt,
        String description
) {
}
