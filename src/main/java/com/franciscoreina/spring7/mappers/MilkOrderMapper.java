package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.domain.order.OrderShipment;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(uses = {Customer.class, OrderLineMapper.class})
public interface MilkOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customer", ignore = true)
    @Mapping(target = "orderLines", ignore = true)
    MilkOrder toEntity(MilkOrderCreateRequest milkOrderCreateRequest);

    @Mapping(target = "customerId", source = "customer")
    @Mapping(target = "orderLineIds", source = "orderLines")
    @Mapping(target = "orderShipmentId", source = "orderShipment")
    MilkOrderResponse toResponse(MilkOrder milkOrder);

    default UUID map(Customer customer) {
        return customer != null ? customer.getId() : null;
    }

    default Set<UUID> map(Set<OrderLine> orderLines) {
        return orderLines == null
                ? Set.of()
                : orderLines.stream()
                .map(OrderLine::getId)
                .collect(Collectors.toSet());
    }

    default UUID map(OrderShipment orderShipment) {
        return orderShipment != null ? orderShipment.getId() : null;
    }
}
