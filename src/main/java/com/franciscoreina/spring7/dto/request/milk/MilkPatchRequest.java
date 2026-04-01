package com.franciscoreina.spring7.dto.request.milk;

import com.franciscoreina.spring7.domain.milk.MilkType;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record MilkPatchRequest(

        @Size(max = 50)
        String name,

        MilkType milkType,

        @Size(max = 50)
        @Pattern(regexp = "^[A-Za-z0-9]+$", message = "Only letters and numbers are allowed")
        String upc,

        @Positive
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @PositiveOrZero
        Integer stock,

        // JPA Relationships

        Set<@NotNull UUID> categoryIds // null = no update, [] = invalid request
) {
}
