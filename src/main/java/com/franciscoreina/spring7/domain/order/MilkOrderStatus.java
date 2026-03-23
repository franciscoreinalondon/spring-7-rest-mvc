package com.franciscoreina.spring7.domain.order;

public enum MilkOrderStatus {
    NEW,                // Draft: can be created and modified
    PLACED,             // Confirmed: stock validation triggered
    READY_FOR_SHIPMENT, // Assigned stock: ready to ship
    SHIPPED,            // Shipped: left warehouse (locked)
    DELIVERED,          // Delivered to customer
    CANCELLED           // Cancelled: stock returned
}
