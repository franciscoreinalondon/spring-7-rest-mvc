package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(@Valid CustomerRequest request);

    CustomerResponse getById(UUID customerId);

    Page<CustomerResponse> search(String name, String email, Pageable pageable);

    CustomerResponse update(UUID customerId, @Valid CustomerRequest request);

    CustomerResponse patch(UUID customerId, @Valid CustomerPatchRequest request);

    void delete(UUID customerId);

}
