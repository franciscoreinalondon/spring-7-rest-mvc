package com.franciscoreina.spring7.dto.request.order;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MilkOrderCreateRequest(
        @NotBlank
        @Size(max = 50)
        String customerRef,

        @NotNull
        @DecimalMin("0.00")
        @Digits(integer = 10, fraction = 2)
        BigDecimal paymentAmount,

        @NotNull
        Customer customer,

        @NotEmpty
        Set<@Valid OrderLineCreateRequest> orderLines
) {
}
