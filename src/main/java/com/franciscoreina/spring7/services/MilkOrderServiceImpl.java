package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.application.kafka.event.OrderPlacedEvent;
import com.franciscoreina.spring7.domain.customer.Customer;
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
import com.franciscoreina.spring7.repositories.OrderLineRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
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
    private final OrderLineRepository orderLineRepository;
    private final OrderLineMapper orderLineMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    // ----------------------------
    //  SERVICE OPERATIONS - ORDER
    // ----------------------------

    @Transactional
    @Override
    public MilkOrderResponse create(MilkOrderRequest request) {
        log.info("Creating milk order with customerRef={}", request.customerRef());

        var customer = findCustomerOrThrow(request.customerId());
        var order = MilkOrder.createMilkOrder(customer, request.customerRef());

        request.orderLines().forEach(lineRequest -> {
            var milk = findMilkOrThrow(lineRequest.milkId());
            var orderLine = OrderLine.createOrderLine(milk, lineRequest.requestedQuantity());
            order.addOrderLine(orderLine);
        });

        var savedOrder = milkOrderRepository.save(order);
        return milkOrderMapper.toResponse(savedOrder);
    }

    @Override
    public MilkOrderResponse getById(UUID milkOrderId) {
        return milkOrderMapper.toResponse(findMilkOrderOrThrow(milkOrderId));
    }

    @Override
    public Page<MilkOrderResponse> search(String customerRef, Pageable pageable) {
        var cleanCustomerRef = normalizeFilter(customerRef);

        log.debug("Listing milk orders with filter: customerRef={}, page={}, size={}",
                cleanCustomerRef, pageable.getPageNumber(), pageable.getPageSize());

        if (cleanCustomerRef != null) {
            return milkOrderRepository.findAllByCustomerRefContainingIgnoreCase(cleanCustomerRef, pageable)
                    .map(milkOrderMapper::toResponse);
        }

        return milkOrderRepository.findAll(pageable)
                .map(milkOrderMapper::toResponse);
    }

    @Transactional
    @Override
    public MilkOrderResponse payOrder(UUID milkOrderId) {
        log.info("Paying milk order with id={}", milkOrderId);

        var order = findMilkOrderOrThrow(milkOrderId);
        var wasPaid = order.isPaid();

        order.markAsPaid();

        if (!wasPaid && order.isPaid()) {
            applicationEventPublisher.publishEvent(new OrderPlacedEvent(order.getId()));
        }

        return milkOrderMapper.toResponse(order);
    }

    // ----------------------------------
    //  SERVICE OPERATIONS - ORDER LINES
    // ----------------------------------

    @Transactional
    @Override
    public OrderLineResponse addLine(UUID milkOrderId, OrderLineCreateRequest request) {
        log.info("Adding order line to milk order id={}", milkOrderId);

        var order = findMilkOrderOrThrow(milkOrderId);
        var milk = findMilkOrThrow(request.milkId());

        var newLine = OrderLine.createOrderLine(milk, request.requestedQuantity());
        order.addOrderLine(newLine);

        // Although OrderLine is part of MilkOrder (cascade = ALL),
        // when the order already exists (managed entity),
        // adding a new child does not guarantee it will be persisted before mapping.
        // We explicitly save it to ensure ID generation and avoid null values in the response.
        var savedLine = orderLineRepository.saveAndFlush(newLine);

        return orderLineMapper.toResponse(savedLine);
    }

    @Transactional
    @Override
    public OrderLineResponse updateLine(UUID milkOrderId, UUID orderLineId, OrderLineUpdateRequest request) {
        log.info("Updating order line quantity for milk order id={}, order line id={}", milkOrderId, orderLineId);

        var order = findMilkOrderOrThrow(milkOrderId);
        var line = findLineOrThrow(order, orderLineId);

        order.updateOrderLineQuantity(line, request.requestedQuantity());
        return orderLineMapper.toResponse(line);
    }

    @Transactional
    @Override
    public void removeLine(UUID milkOrderId, UUID orderLineId) {
        log.info("Removing order line id={} from milk order id={}", orderLineId, milkOrderId);

        var order = findMilkOrderOrThrow(milkOrderId);
        var line = findLineOrThrow(order, orderLineId);

        order.removeOrderLine(line);
    }

    // -----------------
    //  PRIVATE HELPERS
    // -----------------

    private Customer findCustomerOrThrow(UUID customerId) {
        return customerRepository.findById(customerId)
                .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }

    private Milk findMilkOrThrow(UUID milkId) {
        return milkRepository.findById(milkId)
                .orElseThrow(() -> new NotFoundException("Milk not found: " + milkId));
    }

    private MilkOrder findMilkOrderOrThrow(UUID milkOrderId) {
        return milkOrderRepository.findById(milkOrderId)
                .orElseThrow(() -> new NotFoundException("Milk order not found: " + milkOrderId));
    }

    private OrderLine findLineOrThrow(MilkOrder order, UUID lineId) {
        return order.getOrderLines().stream()
                .filter(line -> lineId.equals(line.getId()))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Order line not found: " + lineId));
    }

    private String normalizeFilter(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
