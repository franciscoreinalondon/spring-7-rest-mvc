package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper
public interface CustomerMapper {

    default Customer toEntity(CustomerRequest request) {
        if (request == null) return null;

        return Customer.createCustomer(
                request.name(),
                request.email()
        );
    }

    default void updateEntity(@MappingTarget Customer target, CustomerRequest request) {
        if (target == null || request == null) return;

        target.renameTo(request.name());
        target.changeEmailTo(request.email());
    }

    default void patchEntity(@MappingTarget Customer target, CustomerPatchRequest request) {
        if (target == null || request == null) return;

        if (request.name() != null) target.renameTo(request.name());
        if (request.email() != null) target.changeEmailTo(request.email());
    }

    CustomerResponse toResponse(Customer customer);
}
