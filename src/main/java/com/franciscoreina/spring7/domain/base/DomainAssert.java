package com.franciscoreina.spring7.domain.base;

import java.util.Collection;

public final class DomainAssert {

    private DomainAssert() {
    }

    public static void notNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> collection, String message) {
        if (collection == null || collection.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isNonNegative(Integer value, String message) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isPositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }
}
