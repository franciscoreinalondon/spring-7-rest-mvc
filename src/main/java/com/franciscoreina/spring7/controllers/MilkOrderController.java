package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.services.MilkOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping(ApiPaths.MILK_ORDERS)
public class MilkOrderController {

    private final MilkOrderService milkOrderService;

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody MilkOrderCreateRequest request) {
        var milkOrderResponse = milkOrderService.create(request);
        var location = URI.create(ApiPaths.MILK_ORDERS + "/" + milkOrderResponse.id());

        return ResponseEntity.created(location).build();
    }

    @GetMapping(ApiPaths.MILK_ORDER_ID)
    public MilkOrderResponse getById(@PathVariable("milkOrderId") UUID milkOrderId) {
        return milkOrderService.getById(milkOrderId);
    }

    @GetMapping
    public Page<MilkOrderResponse> list(
            @RequestParam(value = "customerRef", required = false) String customerRef,
            Pageable pageable) {
        return milkOrderService.list(customerRef, pageable);
    }
}
