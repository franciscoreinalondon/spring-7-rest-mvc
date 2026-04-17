package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
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
    public ResponseEntity<MilkOrderResponse> create(@Valid @RequestBody MilkOrderRequest request) {
        log.info("Creating milk order with customer ref: {}", request.customerRef());

        var milkOrderResponse = milkOrderService.create(request);
        var location = URI.create(ApiPaths.MILK_ORDERS + "/" + milkOrderResponse.id());

        return ResponseEntity.created(location).body(milkOrderResponse);
    }

    @GetMapping(ApiPaths.MILK_ORDER_ID)
    public MilkOrderResponse getById(@PathVariable("milkOrderId") UUID milkOrderId) {
        log.info("Getting milk order by id: {}", milkOrderId);

        return milkOrderService.getById(milkOrderId);
    }

    @GetMapping
    public Page<MilkOrderResponse> search(
            @RequestParam(value = "customerRef", required = false) String customerRef,
            Pageable pageable) {
        log.info("Getting all milk orders");

        return milkOrderService.search(customerRef, pageable);
    }

    @PostMapping("/{milkOrderId}/pay")
    public ResponseEntity<MilkOrderResponse> payOrder(@PathVariable("milkOrderId") UUID milkOrderId) {
        log.info("Paying milk order by id: {}", milkOrderId);

        var milkOrderResponse = milkOrderService.payOrder(milkOrderId);

        return ResponseEntity.accepted().body(milkOrderResponse);
    }

    // ---------------
    //   ORDER LINES
    // ---------------

    @PostMapping(ApiPaths.MILK_ORDER_ID + ApiPaths.ORDER_LINES)
    public ResponseEntity<OrderLineResponse> addLine(@PathVariable("milkOrderId") UUID milkOrderId, @Valid @RequestBody OrderLineCreateRequest request) {
        log.info("Adding line for order id: {}", milkOrderId);

        var orderLineResponse = milkOrderService.addLine(milkOrderId, request);
        var location = URI.create(ApiPaths.MILK_ORDERS + "/" + milkOrderId + ApiPaths.ORDER_LINES + "/" + orderLineResponse.id());

        return ResponseEntity.created(location).body(orderLineResponse);
    }

    @PutMapping(ApiPaths.MILK_ORDER_ID + ApiPaths.ORDER_LINES + ApiPaths.ORDER_LINE_ID)
    public OrderLineResponse updateLine(
            @PathVariable("milkOrderId") UUID milkOrderId,
            @PathVariable("orderLineId") UUID orderLineId,
            @Valid @RequestBody OrderLineUpdateRequest request) {
        log.info("Updating quantity for line: {}", orderLineId);

        return milkOrderService.updateLine(milkOrderId, orderLineId, request);
    }

    @DeleteMapping(ApiPaths.MILK_ORDER_ID + ApiPaths.ORDER_LINES + ApiPaths.ORDER_LINE_ID)
    public ResponseEntity<Void> removeLine(
            @PathVariable("milkOrderId") UUID milkOrderId,
            @PathVariable("orderLineId") UUID orderLineId) {
        log.info("Removing line with id: {}", orderLineId);

        milkOrderService.removeLine(milkOrderId, orderLineId);

        return ResponseEntity.noContent().build();
    }
}
