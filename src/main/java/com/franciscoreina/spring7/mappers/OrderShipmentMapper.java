package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.order.OrderShipment;
import com.franciscoreina.spring7.dto.request.order.OrderShipmentCreateRequest;
import com.franciscoreina.spring7.dto.response.order.OrderShipmentResponse;
import org.mapstruct.Mapping;

public interface OrderShipmentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    OrderShipment toEntity(OrderShipmentCreateRequest orderShipmentCreateRequest);

    OrderShipmentResponse toResponse(OrderShipment orderShipment);

}
