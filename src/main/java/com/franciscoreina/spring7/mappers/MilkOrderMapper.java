package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * No update mapping: MilkOrder must be modified through explicit domain operations
 * to enforce aggregate invariants and lifecycle rules.
 */
@Mapper(uses = OrderLineMapper.class)
public interface MilkOrderMapper {

    default MilkOrder toEntity(MilkOrderRequest request, Customer customer) {
        if (request == null || customer == null) return null;

        return MilkOrder.createMilkOrder(
                customer,
                request.customerRef()
        );
    }

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "orderShipmentId", source = "orderShipment.id")
    MilkOrderResponse toResponse(MilkOrder milkOrder);
}
