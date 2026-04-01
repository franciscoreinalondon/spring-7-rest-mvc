package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.UUID;

@Mapper
public interface MilkOrderMapper {

    default MilkOrder toEntity(MilkOrderRequest request, Customer customer) {
        if (request == null || customer == null) return null;

        return MilkOrder.createMilkOrder(
                customer,
                request.customerRef()
        );
    }

    //tbd: update to modify customerRef

    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "orderShipmentId", source = "orderShipment.id")
    MilkOrderResponse toResponse(MilkOrder milkOrder);

    /**
     * MapStruct automatically identifies and applies this helper method by matching
     * the source (OrderLine) and target (UUID) types required to resolve the collection mapping.
     */
    default UUID mapOrderLineToId(OrderLine orderLine) {
        return orderLine == null ? null : orderLine.getId();
    }
}
