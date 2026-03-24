package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * No update mapping: OrderLine must be modified through the aggregate (MilkOrder)
 * to enforce domain invariants.
 */
@Mapper
public interface OrderLineMapper {

    default OrderLine toEntity(OrderLineUpdateRequest request, Milk milk) {
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
