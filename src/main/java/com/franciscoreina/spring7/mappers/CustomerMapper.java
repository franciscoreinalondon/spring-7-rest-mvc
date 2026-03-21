package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper
public interface CustomerMapper {

    default Customer toEntity(CustomerCreateRequest request) {
        if (request == null) return null;

        return Customer.createCustomer(
                request.name(),
                request.email()
        );
    }

//    Customer toEntity(CustomerResponse customerResponse);

//    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.SET_TO_NULL)
//    void updateEntity(@MappingTarget Customer target, CustomerUpdateRequest customerUpdateRequest);

    //    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
//    void patchEntity(@MappingTarget Customer target, CustomerPatchRequest customerPatchRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    default void updateEntity(@MappingTarget Customer target, CustomerUpdateRequest request) {
        if (request == null) return;

        // Llamamos a tus métodos de negocio en lugar de setters
        target.renameTo(request.name());
        target.changeEmailTo(request.email());
    }

    // Para el PATCH (solo actualiza si no es nulo)
    default void patchEntity(@MappingTarget Customer target, CustomerPatchRequest request) {
        if (request == null) return;

        if (request.name() != null) {
            target.renameTo(request.name());
        }
        if (request.email() != null) {
            target.changeEmailTo(request.email());
        }
    }

    CustomerResponse toResponse(Customer customer);

}
