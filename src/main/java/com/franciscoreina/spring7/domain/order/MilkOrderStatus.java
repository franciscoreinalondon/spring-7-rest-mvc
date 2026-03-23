package com.franciscoreina.spring7.domain.order;

public enum MilkOrderStatus {
    NEW,        // Added, stock not reserved
    CONFIRMED,  // Stock allocated, locked
    SHIPPED,    // Left warehouse (final)
    CANCELLED   // Stock returned
}
