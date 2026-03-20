package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MilkOrderServiceImpl implements MilkOrderService {

    @Override
    public MilkOrderResponse create(MilkOrderCreateRequest milkOrderCreateRequest) {
        return null;
    }

    @Override
    public MilkOrderResponse getById(UUID milkOrderId) {
        return null;
    }

    @Override
    public Page<MilkOrderResponse> list(String customerRef, Pageable pageable) {
        return null;
    }
}
