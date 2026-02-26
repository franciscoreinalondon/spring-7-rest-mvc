package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Customer toEntity(CustomerCreateRequest customerCreateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
    void updateEntity(@MappingTarget Customer target, CustomerUpdateRequest customerUpdateRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patchEntity(@MappingTarget Customer target, CustomerPatchRequest customerPatchRequest);

    CustomerResponse toResponse(Customer customer);

}
