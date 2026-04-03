package com.franciscoreina.spring7.dto.request.customer;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerPatchRequest(

        @Pattern(regexp = "^(?!\\s*$).+", message = "Name must not be blank")
        @Size(max = 50)
        String name,

        @Pattern(regexp = "^(?!\\s*$).+", message = "Email must not be blank")
        @Email(message = "Must be a well-formed email address")
        @Size(max = 120)
        String email
) {
}
