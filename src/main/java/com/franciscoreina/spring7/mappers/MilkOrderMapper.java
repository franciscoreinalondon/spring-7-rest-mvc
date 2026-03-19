package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {Customer.class, OrderLineMapper.class})
public interface MilkOrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    MilkOrder toEntity(MilkOrderCreateRequest milkOrderCreateRequest);

    MilkOrderResponse toResponse(MilkOrder milkOrder);

}
