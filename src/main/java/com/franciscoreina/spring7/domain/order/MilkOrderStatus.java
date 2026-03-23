package com.franciscoreina.spring7.domain.order;

public enum MilkOrderStatus {
    NEW,        // Draft: editable, no stock reserved
    CONFIRMED,  // Confirmed: stock allocated, locked
    SHIPPED,    // Shipped: left warehouse (final)
    CANCELLED   // Cancelled: stock returned
}
