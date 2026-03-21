package com.franciscoreina.spring7.dto.request.customer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public record CustomerPatchRequest(
        @Size(max = 50)
        String name,

        @Email(message = "Invalid email format")
        @Size(max = 120)
        String email
) {
}
