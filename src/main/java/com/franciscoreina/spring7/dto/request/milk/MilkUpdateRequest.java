package com.franciscoreina.spring7.dto.request.milk;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.response.milk.CategoryResponse;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MilkUpdateRequest(
        @NotBlank
        @Size(max = 50)
        String name,

        @NotNull
        MilkType milkType,

        @NotBlank
        @Size(max = 50)
        @Pattern(regexp = "^[0-9A-Za-z]+$")
        String upc,

        @NotNull
        @DecimalMin(value = "0.00", inclusive = false)
        @Digits(integer = 10, fraction = 2)
        BigDecimal price,

        @NotNull
        @PositiveOrZero
        Integer stock,

        @NotEmpty
        Set<@NotNull CategoryResponse> categories
) {
}
