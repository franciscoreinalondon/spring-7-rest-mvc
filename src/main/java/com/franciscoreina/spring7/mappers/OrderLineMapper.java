package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * No update mapping: MilkOrder must be modified through explicit domain operations
 * to enforce aggregate invariants and lifecycle rules.
 */
@Mapper
public interface OrderLineMapper {

    default OrderLine toEntity(OrderLineCreateRequest request, Milk milk) {
        if (request == null || milk == null) return null;

        return OrderLine.createOrderLine(
                milk,
                request.requestedQuantity()
        );
    }

    @Mapping(target = "milkOrderId", source = "milkOrder.id")
    @Mapping(target = "milkId", source = "milk.id")
    OrderLineResponse toResponse(OrderLine orderLine);
}
