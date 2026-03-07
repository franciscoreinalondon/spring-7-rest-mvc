package com.franciscoreina.spring7.exceptions;

public record ApiError(
        int status,
        String message
) {
}
