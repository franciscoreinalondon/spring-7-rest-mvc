package com.franciscoreina.spring7.dto.request.milk;

import com.franciscoreina.spring7.domain.milk.MilkType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MilkCreateRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        MilkType milkType,

        @NotBlank
        @Size(max = 50)
        String upc,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull
        @Min(0)
        Integer stock
) {
}
