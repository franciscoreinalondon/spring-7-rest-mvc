package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilkOrderService {
    MilkOrderResponse create(MilkOrderCreateRequest request);

    MilkOrderResponse getById(UUID milkOrderId);

    Page<MilkOrderResponse> list(String customerRef, Pageable pageable);


}

