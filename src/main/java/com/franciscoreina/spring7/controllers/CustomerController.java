package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(ApiPaths.CUSTOMERS)
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        log.info("Creating customer with email={}", request.email());

        var customerResponse = customerService.create(request);
        var location = URI.create(ApiPaths.CUSTOMERS + "/" + customerResponse.id());

        return ResponseEntity.created(location).body(customerResponse);
    }

    @GetMapping(ApiPaths.CUSTOMER_ID)
    public CustomerResponse getById(@PathVariable("customerId") UUID customerId) {
        log.info("Getting customer id={}", customerId);

        return customerService.getById(customerId);
    }

    @GetMapping
    public Page<CustomerResponse> search(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            Pageable pageable) {
        log.info("Searching customers with filters: name={}, email={}", name, email);

        return customerService.search(name, email, pageable);
    }

    @PutMapping(ApiPaths.CUSTOMER_ID)
    public CustomerResponse update(@PathVariable("customerId") UUID customerId,
                                   @Valid @RequestBody CustomerRequest request) {
        log.info("Updating customer id={}", customerId);

        return customerService.update(customerId, request);
    }

    @PatchMapping(ApiPaths.CUSTOMER_ID)
    public CustomerResponse patch(@PathVariable("customerId") UUID customerId,
                                  @Valid @RequestBody CustomerPatchRequest request) {
        log.info("Patching customer id={}", customerId);

        return customerService.patch(customerId, request);
    }

    @DeleteMapping(ApiPaths.CUSTOMER_ID)
    public ResponseEntity<Void> delete(@PathVariable("customerId") UUID customerId) {
        log.info("Deleting customer id={}", customerId);

        customerService.delete(customerId);

        return ResponseEntity.noContent().build();
    }
}
