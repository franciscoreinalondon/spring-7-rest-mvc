package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilkOrderService {

    // Order operations

    MilkOrderResponse create(@Valid MilkOrderRequest request);

    MilkOrderResponse getById(UUID milkOrderId);

    Page<MilkOrderResponse> search(String customerRef, Pageable pageable);

    MilkOrderResponse payOrder(UUID orderId);

    // Order line operations

    OrderLineResponse addLine(UUID milkOrderId, @Valid OrderLineCreateRequest request);

    OrderLineResponse updateLine(UUID milkOrderId, UUID orderLineId, @Valid OrderLineUpdateRequest request);

    void removeLine(UUID milkOrderId, UUID orderLineId);
}

