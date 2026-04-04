package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.ConflictException;
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

import static com.franciscoreina.spring7.domain.customer.Customer.createCustomer;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    // --------------------
    //  SERVICE OPERATIONS
    // --------------------

    @Transactional
    @Override
    public CustomerResponse create(CustomerRequest request) {
        log.info("Creating customer with email={}", request.email());

        assertEmailNotInUse(request.email());

        var customer = createCustomer(request.name(), request.email());
        var savedCustomer = customerRepository.save(customer);

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getById(UUID customerId) {
        return customerMapper.toResponse(findCustomerOrThrow(customerId));
    }

    @Override
    public Page<CustomerResponse> search(String name, String email, Pageable pageable) {
        var cleanName = normalizeFilter(name);
        var cleanEmail = normalizeFilter(email);

        log.debug("Listing customers with filters: name={}, email={}, page={}, size={}",
                cleanName, cleanEmail, pageable.getPageNumber(), pageable.getPageSize());

        if (cleanName != null && cleanEmail != null) {
            return customerRepository.findAllByNameContainingIgnoreCaseAndEmailIgnoreCase(cleanName, cleanEmail, pageable)
                    .map(customerMapper::toResponse);
        }

        if (cleanName != null) {
            return customerRepository.findAllByNameContainingIgnoreCase(cleanName, pageable)
                    .map(customerMapper::toResponse);
        }

        if (cleanEmail != null) {
            return customerRepository.findAllByEmailIgnoreCase(cleanEmail, pageable)
                    .map(customerMapper::toResponse);
        }

        return customerRepository.findAll(pageable)
                .map(customerMapper::toResponse);
    }

    @Transactional
    @Override
    public CustomerResponse update(UUID customerId, CustomerRequest request) {
        log.info("Updating customer id={}", customerId);

        var customer = findCustomerOrThrow(customerId);

        if (!customer.getEmail().equalsIgnoreCase(request.email())) {
            assertEmailNotInUseByAnotherCustomer(customerId, request.email());
        }

        customerMapper.updateEntity(customer, request);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    @Override
    public CustomerResponse patch(UUID customerId, CustomerPatchRequest request) {
        log.info("Patching customer id={}", customerId);

        var customer = findCustomerOrThrow(customerId);

        if (request.name() == null && request.email() == null) {
            return customerMapper.toResponse(customer);
        }

        if (request.email() != null && !customer.getEmail().equalsIgnoreCase(request.email())) {
            assertEmailNotInUseByAnotherCustomer(customerId, request.email());
        }

        customerMapper.patchEntity(customer, request);
        return customerMapper.toResponse(customer);
    }

    @Transactional
    @Override
    public void delete(UUID customerId) {
        log.info("Deleting customer id={}", customerId);

        var savedCustomer = findCustomerOrThrow(customerId);
        customerRepository.delete(savedCustomer);
    }

    // -----------------
    //  PRIVATE HELPERS
    // -----------------

    private void assertEmailNotInUse(String email) {
        if (customerRepository.existsByEmailIgnoreCase(email.trim())) {
            throw new ConflictException("Customer email already exists: " + email);
        }
    }

    private void assertEmailNotInUseByAnotherCustomer(UUID customerId, String email) {
        var existing = customerRepository.findByEmailIgnoreCase(email.trim());

        if (existing.isPresent() && !existing.get().getId().equals(customerId)) {
            throw new ConflictException("Customer email already exists: " + email);
        }
    }

    private Customer findCustomerOrThrow(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
