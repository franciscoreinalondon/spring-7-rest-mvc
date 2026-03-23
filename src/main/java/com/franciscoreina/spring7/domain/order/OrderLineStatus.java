package com.franciscoreina.spring7.domain.order;

public enum OrderLineStatus {
    NEW,           // Added to order, not yet validated
    RESERVED ,     // Stock reserved (assignedQuantity > 0)
    OUT_OF_STOCK,  // Not enough stock available
    FULFILLED,     // Ready to ship (fully allocated)
    CANCELLED      // Line cancelled
}
