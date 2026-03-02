package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;
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
    public CustomerResponse create(CustomerCreateRequest request) {
        Customer saved = customerRepository.save(customerMapper.toEntity(request));
        return customerMapper.toResponse(saved);
    }

    @Override
    public CustomerResponse getById(UUID customerId) {
        Customer customer = getCustomerOrThrow(customerId);
        return customerMapper.toResponse(customer);
    }

    @Override
    public List<CustomerResponse> list() {
        return customerRepository.findAll().stream().map(customerMapper::toResponse).toList();
    }

    @Transactional
    @Override
    public void update(UUID customerId, CustomerUpdateRequest request) {
        Customer customerToUpdate = getCustomerOrThrow(customerId);
        customerMapper.updateEntity(customerToUpdate, request);
        customerRepository.save(customerToUpdate);
    }

    @Transactional
    @Override
    public void patch(UUID customerId, CustomerPatchRequest request) {
        Customer customerToPatch = getCustomerOrThrow(customerId);
        customerMapper.patchEntity(customerToPatch, request);
        customerRepository.save(customerToPatch);
    }

    @Override
    public void delete(UUID customerId) {
        Customer customer = getCustomerOrThrow(customerId);
        customerRepository.delete(customer);
    }

    private Customer getCustomerOrThrow(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }
}
