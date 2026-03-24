package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(UUID customerId);

    Page<CustomerResponse> list(String name, String email, Pageable pageable);

    CustomerResponse update(UUID customerId, CustomerRequest request);

    CustomerResponse patch(UUID customerId, CustomerPatchRequest request);

    void delete(UUID customerId);

}
