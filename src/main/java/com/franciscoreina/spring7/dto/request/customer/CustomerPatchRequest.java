package com.franciscoreina.spring7.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record CustomerPatchRequest(

        @Size(max = 50)
        String name,

        @Email(message = "Must be a well-formed email address")
        @Size(max = 120)
        String email
) {
}
