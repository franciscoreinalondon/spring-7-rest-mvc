package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface MilkOrderService {
    MilkOrderResponse create(MilkOrderCreateRequest milkOrderCreateRequest);

    MilkOrderResponse getById(UUID milkOrderId);

    Page<MilkOrderResponse> list(String name, MilkType milkType, Pageable pageable);


}

