package com.franciscoreina.spring7.domain.order;

public enum OrderLineStatus {
    NEW,          // Added, stock not checked
    ALLOCATED,    // Stock reserved (full or partial)
    OUT_OF_STOCK  // No stock available
}
