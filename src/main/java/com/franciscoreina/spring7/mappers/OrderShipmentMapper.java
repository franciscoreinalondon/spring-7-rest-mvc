package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.order.OrderShipment;
import com.franciscoreina.spring7.dto.request.order.OrderShipmentRequest;
import com.franciscoreina.spring7.dto.response.order.OrderShipmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * No toEntity mapping: OrderShipment must be created and assigned through the aggregate (MilkOrder)
 * to enforce domain invariants and ensure a single shipment per order.
 */
@Mapper
public interface OrderShipmentMapper {

    default void updateEntity(@MappingTarget OrderShipment target, OrderShipmentRequest request) {
        if (target == null || request == null) return;

        target.updateTrackingNumber(request.trackingNumber());
    }

    @Mapping(target = "milkOrderId", source = "milkOrder.id")
    OrderShipmentResponse toResponse(OrderShipment orderShipment);
}
