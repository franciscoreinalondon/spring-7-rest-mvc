package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.CustomerMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse create(CustomerRequest request) {
        var savedCustomer = customerRepository.save(customerMapper.toEntity(request));
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getById(UUID customerId) {
        var savedCustomer = getCustomerOrThrow(customerId);
        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public List<CustomerResponse> list() {
        return customerRepository.findAll().stream().map(customerMapper::toResponse).toList();
    }

    @Transactional
    @Override
    public void update(UUID customerId, CustomerRequest request) {
        var customerToUpdate = getCustomerOrThrow(customerId);
        customerMapper.updateEntity(customerToUpdate, request);
        customerRepository.save(customerToUpdate);
    }

    @Transactional
    @Override
    public void patch(UUID customerId, CustomerPatchRequest request) {
        var customerToPatch = getCustomerOrThrow(customerId);
        customerMapper.patchEntity(customerToPatch, request);
        customerRepository.save(customerToPatch);
    }

    @Override
    public void delete(UUID customerId) {
        var savedCustomer = getCustomerOrThrow(customerId);
        customerRepository.delete(savedCustomer);
    }

    private Customer getCustomerOrThrow(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }
}
