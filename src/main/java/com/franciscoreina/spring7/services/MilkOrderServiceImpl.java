package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.MilkOrderMapper;
import com.franciscoreina.spring7.mappers.OrderLineMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class MilkOrderServiceImpl implements MilkOrderService {

    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkOrderRepository milkOrderRepository;
    private final MilkOrderMapper milkOrderMapper;
    private final OrderLineMapper orderLineMapper;

    // ---------------
    //      ORDER
    // ---------------

    @Transactional
    @Override
    public MilkOrderResponse create(MilkOrderRequest request) {
        log.info("Creating milk order: {}", request.customerRef());
        var customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new NotFoundException("Customer not found: " + request.customerId()));

        var order = milkOrderMapper.toEntity(request, customer);

        request.orderLines().forEach(line -> {
            var milk = milkRepository.findById(line.milkId())
                    .orElseThrow(() -> new NotFoundException("Milk not found: " + line.milkId()));

            order.addOrderLine(OrderLine.createOrderLine(milk, line.requestedQuantity()));
        });

        return milkOrderMapper.toResponse(milkOrderRepository.save(order));
    }

    @Override
    public MilkOrderResponse getById(UUID milkOrderId) {
        return milkOrderMapper.toResponse(findMilkOrderOrThrow(milkOrderId));
    }

    @Override
    public Page<MilkOrderResponse> list(String customerRef, Pageable pageable) {
        String cleanCustomerRef = (customerRef != null && !customerRef.isBlank()) ? customerRef.trim() : null;

        if (cleanCustomerRef != null) {  // Search by customerRef
            return milkOrderRepository.findAllByCustomerRefContainingIgnoreCase(cleanCustomerRef, pageable)
                    .map(milkOrderMapper::toResponse);
        }

        return milkOrderRepository.findAll(pageable) // Search all
                .map(milkOrderMapper::toResponse);
    }

    // ---------------
    //      ORDER
    // ---------------

    @Transactional
    @Override
    public OrderLineResponse addLine(UUID milkOrderId, OrderLineCreateRequest request) {
        var order = findMilkOrderOrThrow(milkOrderId);
        var milk = findMilkOrThrow(request.milkId());
        var newLine = OrderLine.createOrderLine(milk, request.requestedQuantity());

        order.addOrderLine(newLine);
        milkOrderRepository.save(order); // Hibernate persists via dirty checking; explicit save added for tests.
        return orderLineMapper.toResponse(newLine);
    }

    @Transactional
    @Override
    public OrderLineResponse updateLineQuantity(UUID milkOrderId, UUID orderLineId, OrderLineUpdateRequest request) {
        var order = findMilkOrderOrThrow(milkOrderId);
        var line = findLineOrThrow(order, orderLineId);

        order.updateOrderLineQuantity(line, request.requestedQuantity());
        milkOrderRepository.save(order); // Hibernate persists via dirty checking; explicit save added for tests.
        return orderLineMapper.toResponse(line);
    }

    @Transactional
    @Override
    public void removeLine(UUID milkOrderId, UUID orderLineId) {
        var order = findMilkOrderOrThrow(milkOrderId);
        var line = findLineOrThrow(order, orderLineId);

        order.removeOrderLine(line);
        milkOrderRepository.save(order); // Hibernate persists via dirty checking; explicit save added for tests.
    }

    private Milk findMilkOrThrow(UUID milkId) {
        return milkRepository.findById(milkId)
                .orElseThrow(() -> new NotFoundException("Milk not found: " + milkId));
    }

    private MilkOrder findMilkOrderOrThrow(UUID milkOrderId) {
        return milkOrderRepository.findById(milkOrderId)
                .orElseThrow(() -> new NotFoundException("MilkOrder not found: " + milkOrderId));
    }

    private OrderLine findLineOrThrow(MilkOrder order, UUID lineId) {
        return order.getOrderLines().stream()
                .filter(line -> line.getId().equals(lineId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("OrderLine not found: " + lineId));
    }
}
