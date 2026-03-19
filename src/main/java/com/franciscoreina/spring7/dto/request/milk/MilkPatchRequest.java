package com.franciscoreina.spring7.dto.request.milk;

import com.franciscoreina.spring7.domain.milk.MilkType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MilkPatchRequest(
        @Size(max = 50)
        String name,

        MilkType milkType,

        @Size(max = 50)
        String upc,

        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @PositiveOrZero
        Integer stock
) {
}
