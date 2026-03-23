package com.franciscoreina.spring7.dto.response.customer;

import java.time.Instant;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        Instant createdAt,
        String name,
        String email
) {
}
