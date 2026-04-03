package com.franciscoreina.spring7.domain.base;

import java.math.BigDecimal;
import java.util.Collection;

public final class DomainAssert {

    private DomainAssert() {
    }

    public static <T> T notNull(T value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
        return value;
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

    public static void isPositiveOrZero(Integer value, String message) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isPositive(Integer value, String message) {
        if (value == null || value <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isPositiveOrZero(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isPositive(BigDecimal value, String message) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(message);
        }
    }

    public static <T> void cannotRemoveOnlyElement(Collection<T> collection, T element, String message) {
        if (collection != null && collection.contains(element) && collection.size() <= 1) {
            throw new IllegalStateException(message);
        }
    }
}
