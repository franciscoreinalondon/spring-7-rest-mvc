package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.services.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiPaths.CUSTOMERS)
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody CustomerCreateRequest request) {
        CustomerResponse customerResponse = customerService.create(request);

        URI location = URI.create(ApiPaths.CUSTOMERS + "/" + customerResponse.id());
        return ResponseEntity.created(location).build();
    }

    @GetMapping(ApiPaths.CUSTOMER_ID)
    public CustomerResponse getById(@PathVariable("customerId") UUID customerId) {
        return customerService.getById(customerId);
    }

    @GetMapping
    public List<CustomerResponse> list() {
        return customerService.list();
    }

    @PutMapping(ApiPaths.CUSTOMER_ID)
    public ResponseEntity<Void> update(@PathVariable("customerId") UUID customerId, @Valid @RequestBody CustomerUpdateRequest request) {
        customerService.update(customerId, request);

        return ResponseEntity.noContent().build();
    }

    @PatchMapping(ApiPaths.CUSTOMER_ID)
    public ResponseEntity<Void> patch(@PathVariable("customerId") UUID customerId, @Valid @RequestBody CustomerPatchRequest request) {
        customerService.patch(customerId, request);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(ApiPaths.CUSTOMER_ID)
    public ResponseEntity<Void> delete(@PathVariable("customerId") UUID customerId) {
        customerService.delete(customerId);

        return ResponseEntity.noContent().build();
    }
}
