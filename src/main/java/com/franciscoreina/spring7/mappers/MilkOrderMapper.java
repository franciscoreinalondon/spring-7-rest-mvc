package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.domain.order.OrderShipment;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ObjectFactory;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(uses = {Customer.class})
public interface MilkOrderMapper {

    @ObjectFactory
    default MilkOrder createMilkOrder(MilkOrderCreateRequest request, @Context Customer customer) {
        return MilkOrder.createMilkOrder(customer, request.customerRef());
    }

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

//
//    @AfterMapping
//    default void addLines(MilkOrderCreateRequest request, @MappingTarget MilkOrder order) {
//        if (request.getOrderLines() != null) {
//            request.getOrderLines().forEach(lineDto -> {
//                // Aquí llamarías a tu lógica para convertir DTO a OrderLine
//                // y usarías order.addOrderLine(linea);
//                // Esto asegura que el paymentAmount se calcule bien automáticamente
//            });
//        }
//    }
}
