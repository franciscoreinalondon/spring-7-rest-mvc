package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {Milk.class})
public interface OrderLineMapper {

//    @Mapping(target = "id", ignore = true)
//    @Mapping(target = "version", ignore = true)
//    @Mapping(target = "createdAt", ignore = true)
//    @Mapping(target = "updatedAt", ignore = true)
//    OrderLine toEntity(OrderLineCreateRequest orderLineCreateRequest);

    OrderLineResponse toResponse(OrderLine orderLine);

}
