package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse getById(UUID customerId);

    List<CustomerResponse> list();

    void update(UUID customerId, CustomerRequest request);

    void patch(UUID customerId, CustomerPatchRequest request);

    void delete(UUID customerId);

}
