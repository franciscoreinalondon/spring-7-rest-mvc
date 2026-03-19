package com.franciscoreina.spring7.dto.response.milk;

import com.franciscoreina.spring7.domain.milk.MilkType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

public record MilkResponse(
        UUID id,
        Integer version,
        String name,
        MilkType milkType,
        String upc,
        BigDecimal price,
        Integer stock,
        Instant createdAt,
        Instant updatedAt,
        Set<UUID> categoryIds
) {
}
