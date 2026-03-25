package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.CustomerMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Transactional
    @Override
    public CustomerResponse create(CustomerRequest request) {
        log.info("Creating customer: {}", request.email());
        var customer = customerMapper.toEntity(request);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    @Override
    public CustomerResponse getById(UUID customerId) {
        return customerMapper.toResponse(findCustomerOrThrow(customerId));
    }

    @Override
    public Page<CustomerResponse> list(String name, String email, Pageable pageable) {
        String cleanName = (name != null && !name.isBlank()) ? name.trim() : null;
        String cleanEmail = (email != null && !email.isBlank()) ? email.trim() : null;

        if (cleanName != null && cleanEmail != null) { // Search by name and email
            return customerRepository.findAllByNameContainingIgnoreCaseAndEmailIgnoreCase(cleanName, cleanEmail, pageable)
                    .map(customerMapper::toResponse);
        }

        if (cleanName != null) {  // Search by name
            return customerRepository.findAllByNameContainingIgnoreCase(cleanName, pageable)
                    .map(customerMapper::toResponse);
        }

        if (cleanEmail != null) {  // Search by email
            return customerRepository.findAllByEmailIgnoreCase(cleanEmail, pageable)
                    .map(customerMapper::toResponse);
        }

        return customerRepository.findAll(pageable)  // Search all
                .map(customerMapper::toResponse);
    }

    @Transactional
    @Override
    public CustomerResponse update(UUID customerId, CustomerRequest request) {
        log.info("Updating customer id: {}", customerId);
        var customer = findCustomerOrThrow(customerId);
        customerMapper.updateEntity(customer, request);
        customerRepository.save(customer); // Hibernate persists via dirty checking; explicit save added for tests.
        return customerMapper.toResponse(customer);
    }

    @Transactional
    @Override
    public CustomerResponse patch(UUID customerId, CustomerPatchRequest request) {
        log.info("Patching customer id: {}", customerId);
        var customer = findCustomerOrThrow(customerId);
        customerMapper.patchEntity(customer, request);
        customerRepository.save(customer); // Hibernate persists via dirty checking; explicit save added for tests.
        return customerMapper.toResponse(customer);
    }

    @Transactional
    @Override
    public void delete(UUID customerId) {
        log.info("Deleting customer id: {}", customerId);
        var savedCustomer = findCustomerOrThrow(customerId);
        customerRepository.delete(savedCustomer);
    }

    private Customer findCustomerOrThrow(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }
}
