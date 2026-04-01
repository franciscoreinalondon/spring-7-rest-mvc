package com.franciscoreina.spring7.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRequest(

        @NotBlank
        @Size(max = 50)
        String name,

        @NotBlank
        @Email(message = "Must be a well-formed email address")
        @Size(max = 120)
        String email
) {
}
