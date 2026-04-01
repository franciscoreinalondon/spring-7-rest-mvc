package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderShipment;
import com.franciscoreina.spring7.dto.request.order.OrderShipmentRequest;
import com.franciscoreina.spring7.dto.response.order.OrderShipmentResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface OrderShipmentMapper {

//    default OrderShipment toEntity(OrderShipmentRequest request, MilkOrder milkOrder) {
//        if (request == null || milkOrder == null) return null;
//
//        return OrderShipment.createOrderShipment(
//                request.trackingNumber(),
//                milkOrder
//        );
//    }

    default void updateEntity(@MappingTarget OrderShipment target, OrderShipmentRequest request) {
        if (target == null || request == null) return;

        target.updateTrackingNumber(request.trackingNumber());
    }

    @Mapping(target = "milkOrderId", source = "milkOrder.id")
    OrderShipmentResponse toResponse(OrderShipment orderShipment);
}
