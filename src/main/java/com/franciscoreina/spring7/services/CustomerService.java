package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CustomerCreateRequest request);

    CustomerResponse getById(UUID customerId);

    List<CustomerResponse> list();

    void update(UUID customerId, CustomerUpdateRequest request);

    void patch(UUID customerId, CustomerPatchRequest request);

    void delete(UUID customerId);

}
