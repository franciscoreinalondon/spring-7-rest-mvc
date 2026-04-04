package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * No update mapping: MilkOrder must be modified through explicit domain operations
 * to enforce aggregate invariants and lifecycle rules.
 */
@Mapper(uses = OrderLineMapper.class)
public interface MilkOrderMapper {

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "orderShipmentId", source = "orderShipment.id")
    MilkOrderResponse toResponse(MilkOrder milkOrder);
}
