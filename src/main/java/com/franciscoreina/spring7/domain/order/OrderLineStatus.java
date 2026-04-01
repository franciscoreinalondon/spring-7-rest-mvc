package com.franciscoreina.spring7.domain.order;

public enum OrderLineStatus {
    NEW,                    // Added, stock not checked
    PARTIALLY_ALLOCATED,    // Stock partially reserved
    FULLY_ALLOCATED,        // Stock fully reserved
    OUT_OF_STOCK            // No stock available
}
