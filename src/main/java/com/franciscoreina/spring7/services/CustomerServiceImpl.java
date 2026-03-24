package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.CustomerMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Page<CustomerResponse> list(String name, String email, Pageable pageable) {
        return customerRepository.findAll(pageable).map(customerMapper::toResponse);
    }

    @Transactional
    @Override
    public CustomerResponse update(UUID customerId, CustomerRequest request) {
        var customerToUpdate = getCustomerOrThrow(customerId);
        customerMapper.updateEntity(customerToUpdate, request);
        customerRepository.save(customerToUpdate);
        return customerMapper.toResponse(customerToUpdate);
    }

    @Transactional
    @Override
    public CustomerResponse patch(UUID customerId, CustomerPatchRequest request) {
        var customerToPatch = getCustomerOrThrow(customerId);
        customerMapper.patchEntity(customerToPatch, request);
        customerRepository.save(customerToPatch);
        return customerMapper.toResponse(customerToPatch);
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
