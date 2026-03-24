package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import com.franciscoreina.spring7.services.MilkOrderService;
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
@RequestMapping(ApiPaths.MILK_ORDERS)
public class MilkOrderController {

    private final MilkOrderService milkOrderService;

    // ---------------
    //      ORDER
    // ---------------

    @PostMapping
    public ResponseEntity<Void> create(@Valid @RequestBody MilkOrderRequest request) {
        log.info("Creating milk order with customer ref: {}", request.customerRef());

        var milkOrderResponse = milkOrderService.create(request);
        var location = URI.create(ApiPaths.MILK_ORDERS + "/" + milkOrderResponse.id());

        return ResponseEntity.created(location).build();
    }

    @GetMapping(ApiPaths.MILK_ORDER_ID)
    public MilkOrderResponse getById(@PathVariable("milkOrderId") UUID milkOrderId) {
        log.info("Getting milk order by id: {}", milkOrderId);

        return milkOrderService.getById(milkOrderId);
    }

    @GetMapping
    public Page<MilkOrderResponse> list(
            @RequestParam(value = "customerRef", required = false) String customerRef,
            Pageable pageable) {
        log.info("Getting all milk orders");

        return milkOrderService.list(customerRef, pageable);
    }

    // ---------------
    //   ORDER LINES
    // ---------------

    @PostMapping(ApiPaths.MILK_ID + ApiPaths.LINES)
    public ResponseEntity<Void> create(@PathVariable("milkOrderId") UUID milkOrderId, @Valid @RequestBody OrderLineCreateRequest request) {
        log.info("Creating order line for milk order id: {}", milkOrderId);

        var orderLineResponse = milkOrderService.create(milkOrderId, request);
        var location = URI.create(ApiPaths.MILK_ORDERS + "/" + milkOrderId + "/" + ApiPaths.LINES + orderLineResponse.id());

        return ResponseEntity.created(location).build();
    }

    @PatchMapping(ApiPaths.MILK_ID + ApiPaths.LINES + ApiPaths.LINE_ID)
    public OrderLineResponse patch(@PathVariable("lineId") UUID orderLineId, @Valid @RequestBody OrderLineCreateRequest request) {
        log.info("Patching order line with id: {}", orderLineId);

        return milkOrderService.patch(orderLineId, request);
    }

    @DeleteMapping(ApiPaths.MILK_ID + ApiPaths.LINES + ApiPaths.LINE_ID)
    public ResponseEntity<Void> delete(@PathVariable("lineId") UUID orderLineId) {
        log.info("Deleting order line with id: {}", orderLineId);
        milkOrderService.delete(orderLineId);

        return ResponseEntity.noContent().build();
    }
}
