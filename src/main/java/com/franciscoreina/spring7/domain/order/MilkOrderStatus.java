package com.franciscoreina.spring7.domain.order;

public enum MilkOrderStatus {
    NEW,        // Added, stock not reserved
    CONFIRMED,  // Stock allocated, locked
    PAID,       // Paid, placed
    SHIPPED,    // Left warehouse (final)
    CANCELLED   // Stock returned
}
