package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkOrderMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class MilkOrderServiceImpl implements MilkOrderService {

    private final CustomerRepository customerRepository;

    private final MilkRepository milkRepository;

    private final MilkOrderRepository milkOrderRepository;
    private final MilkOrderMapper milkOrderMapper;

    @Override
    public MilkOrderResponse create(MilkOrderCreateRequest request) {
        var savedCustomer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found: " + request.customerId()));

        var milkOrder = milkOrderMapper.createMilkOrder(request, savedCustomer);
//        milkOrder.setCustomer(savedCustomer);//tbf

        for (var lineRequest : request.orderLines()) {
            var milk = milkRepository.findById(lineRequest.milkId())
                    .orElseThrow(() -> new NotFoundException("Milk not found: " + lineRequest.milkId()));

            var orderLine = OrderLine.createOrderLine(milk, lineRequest.orderQuantity());
            milkOrder.addOrderLine(orderLine);
        }

        var savedMilkOrder = milkOrderRepository.save(milkOrder);

        return milkOrderMapper.toResponse(savedMilkOrder);
    }

    @Override
    public MilkOrderResponse getById(UUID milkOrderId) {
        var savedMilkOrder = getMilkOrThrow(milkOrderId);
        return milkOrderMapper.toResponse(savedMilkOrder);//no se mapea los objetos a ids.
    }

    @Override
    public Page<MilkOrderResponse> list(String customerRef, Pageable pageable) {
        if (customerRef != null) {
            return milkOrderRepository.findAllByCustomerRefContainingIgnoreCase(customerRef, pageable)
                    .map(milkOrderMapper::toResponse);
        }

        return milkOrderRepository.findAll(pageable) // Search all
                .map(milkOrderMapper::toResponse);
    }

    private MilkOrder getMilkOrThrow(UUID milkOrderId) {
        return milkOrderRepository.findById(milkOrderId)
                .orElseThrow(() -> new NotFoundException("MilkOrder not found: " + milkOrderId));
    }
}
