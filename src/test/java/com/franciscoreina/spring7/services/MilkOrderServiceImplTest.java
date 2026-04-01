package com.franciscoreina.spring7.services;

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
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MilkOrderServiceImplTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    MilkRepository milkRepository;
    @Mock
    MilkOrderRepository milkOrderRepository;
    @Mock
    MilkOrderMapper milkOrderMapper;
    @Mock
    OrderLineMapper orderLineMapper;
    @InjectMocks
    MilkOrderServiceImpl milkOrderService;


    private static final Model<OrderLineCreateRequest> LINE_CREATE_MODEL = Instancio.of(OrderLineCreateRequest.class)
            .set(field(OrderLineCreateRequest::requestedQuantity), 2)
            .toModel();

    private static final Model<OrderLineUpdateRequest> LINE_UPDATE_MODEL = Instancio.of(OrderLineUpdateRequest.class)
            .set(field(OrderLineUpdateRequest::requestedQuantity), 3)
            .toModel();

    // ---------------
    //    POSITIVE
    // ---------------

    @Nested
    class PositiveTests {

        @Test
        void create_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);

            var lineRequest = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milk.getId())
                    .create();

            var request = new MilkOrderRequest("REF-123", customer.getId(), Set.of(lineRequest));

            var order = MilkOrder.createMilkOrder(customer, request.customerRef());
            var expectedResponse = Instancio.create(MilkOrderResponse.class);

            given(customerRepository.findById(request.customerId())).willReturn(Optional.of(customer));
            given(milkRepository.findById(lineRequest.milkId())).willReturn(Optional.of(milk));
            given(milkOrderMapper.toEntity(request, customer)).willReturn(order);
            given(milkOrderRepository.save(order)).willReturn(order);
            given(milkOrderMapper.toResponse(order)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).save(order);
        }

        @Test
        void getById_shouldReturnResponse_whenExists() {
            // Arrange
            var order = Instancio.create(MilkOrder.class);
            var expectedResponse = Instancio.create(MilkOrderResponse.class);

            given(milkOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
            given(milkOrderMapper.toResponse(order)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.getById(order.getId());

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).findById(order.getId());
        }

        @Test
        void list_shouldReturnPageOfResponse_whenFilteredByCustomerRef() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var order1 = Instancio.create(MilkOrder.class);
            var order2 = Instancio.create(MilkOrder.class);
            var response1 = Instancio.create(MilkOrderResponse.class);
            var response2 = Instancio.create(MilkOrderResponse.class);
            var page = new PageImpl<>(List.of(order1, order2), pageable, 2);

            given(milkOrderRepository.findAllByCustomerRefContainingIgnoreCase("REF123", pageable)).willReturn(page);
            given(milkOrderMapper.toResponse(order1)).willReturn(response1);
            given(milkOrderMapper.toResponse(order2)).willReturn(response2);

            // Act
            var result = milkOrderService.list(" REF123 ", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            verify(milkOrderRepository).findAllByCustomerRefContainingIgnoreCase("REF123", pageable);
        }

        @Test
        void list_shouldReturnPageOfResponse_whenNoFilter() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var order1 = Instancio.create(MilkOrder.class);
            var order2 = Instancio.create(MilkOrder.class);
            var response1 = Instancio.create(MilkOrderResponse.class);
            var response2 = Instancio.create(MilkOrderResponse.class);
            var page = new PageImpl<>(List.of(order1, order2), pageable, 2);

            given(milkOrderRepository.findAll(pageable)).willReturn(page);
            given(milkOrderMapper.toResponse(order1)).willReturn(response1);
            given(milkOrderMapper.toResponse(order2)).willReturn(response2);

            // Act
            var result = milkOrderService.list(null, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(2);
            verify(milkOrderRepository).findAll(pageable);
        }

        @Test
        void addLine_shouldReturnResponse_whenOrderAndMilkExist() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var request = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milk.getId())
                    .create();
            var expectedResponse = Instancio.create(OrderLineResponse.class);

            given(milkOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
            given(milkRepository.findById(request.milkId())).willReturn(Optional.of(milk));
            given(milkOrderRepository.save(order)).willReturn(order);
            given(orderLineMapper.toResponse(any(OrderLine.class))).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.addLine(order.getId(), request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).save(order);
            verify(orderLineMapper).toResponse(any(OrderLine.class));
        }

        @Test
        void updateLineQuantity_shouldReturnResponse_whenLineExists() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();

            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var line = OrderLine.createOrderLine(milk, 2);
            ReflectionTestUtils.setField(line, "id", lineId);
            order.addOrderLine(line);

            var request = Instancio.create(LINE_UPDATE_MODEL);
            var expectedResponse = Instancio.create(OrderLineResponse.class);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkOrderRepository.save(order)).willReturn(order);
            given(orderLineMapper.toResponse(line)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.updateLineQuantity(orderId, lineId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).save(order);
            verify(orderLineMapper).toResponse(line);
        }

        // tbr
//        @Test
        void removeLine_shouldCallRepository_whenLineExists() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();

            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var line = OrderLine.createOrderLine(milk, 2);
            ReflectionTestUtils.setField(line, "id", lineId);
            order.addOrderLine(line);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkOrderRepository.save(order)).willReturn(order);

            // Act
            milkOrderService.removeLine(orderId, lineId);

            // Assert
            verify(milkOrderRepository).save(order);
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Test
        void create_shouldThrowException_whenCustomerNotFound() {
            // Arrange
            var customerId = UUID.randomUUID();
            var milkId = UUID.randomUUID();

            var lineRequest = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milkId)
                    .create();

            var request = new MilkOrderRequest("REF-123", customerId, Set.of(lineRequest));

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.create(request))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(customerId);
            verifyNoInteractions(milkRepository, milkOrderRepository, milkOrderMapper, orderLineMapper);
        }

        @Test
        void create_shouldThrowException_whenMilkNotFound() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var milkId = UUID.randomUUID();

            var lineRequest = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milkId)
                    .create();

            var request = new MilkOrderRequest("REF-123", customer.getId(), Set.of(lineRequest));

            var order = MilkOrder.createMilkOrder(customer, request.customerRef());

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(milkOrderMapper.toEntity(request, customer)).willReturn(order);
            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.create(request))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(customer.getId());
            verify(milkRepository).findById(milkId);
        }

        @Test
        void create_shouldThrowException_whenDataViolation() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);

            var lineRequest = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milk.getId())
                    .create();

            var request =  new MilkOrderRequest("REF-123", customer.getId(), Set.of(lineRequest));

            var order = MilkOrder.createMilkOrder(customer, request.customerRef());

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(milkOrderMapper.toEntity(request, customer)).willReturn(order);
            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkOrderRepository.save(order)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.create(request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkOrderRepository).save(order);
        }

        @Test
        void getById_shouldThrowException_whenNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.getById(orderId))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
        }

        @Test
        void addLine_shouldThrowException_whenOrderNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();
            var request = Instancio.create(OrderLineCreateRequest.class);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.addLine(orderId, request))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
            verifyNoInteractions(milkRepository, orderLineMapper);
        }

        @Test
        void addLine_shouldThrowException_whenMilkNotFound() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var milkId = UUID.randomUUID();

            var request = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milkId)
                    .create();

            given(milkOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
            given(milkRepository.findById(milkId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.addLine(order.getId(), request))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(order.getId());
            verify(milkRepository).findById(milkId);
        }

        @Test
        void addLine_shouldThrowException_whenDataViolation() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");

            var request = Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::milkId), milk.getId())
                    .create();

            given(milkOrderRepository.findById(order.getId())).willReturn(Optional.of(order));
            given(milkRepository.findById(milk.getId())).willReturn(Optional.of(milk));
            given(milkOrderRepository.save(order)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.addLine(order.getId(), request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkOrderRepository).save(order);
        }

        @Test
        void updateLineQuantity_shouldThrowException_whenOrderNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();
            var request = Instancio.create(OrderLineUpdateRequest.class);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.updateLineQuantity(orderId, lineId, request))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
        }

        @Test
        void updateLineQuantity_shouldThrowException_whenLineNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();
            var request = Instancio.create(OrderLineUpdateRequest.class);
            var customer = Instancio.create(Customer.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.updateLineQuantity(orderId, lineId, request))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
        }

        @Test
        void updateLineQuantity_shouldThrowException_whenDataViolation() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();

            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var line = OrderLine.createOrderLine(milk, 2);
            ReflectionTestUtils.setField(line, "id", lineId);
            order.addOrderLine(line);

            var request = Instancio.create(LINE_UPDATE_MODEL);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkOrderRepository.save(order)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.updateLineQuantity(orderId, lineId, request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkOrderRepository).save(order);
        }

        @Test
        void removeLine_shouldThrowException_whenOrderNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.removeLine(orderId, lineId))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
        }

        @Test
        void removeLine_shouldThrowException_whenLineNotFound() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();
            var customer = Instancio.create(Customer.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.removeLine(orderId, lineId))
                    .isInstanceOf(NotFoundException.class);

            verify(milkOrderRepository).findById(orderId);
        }

        // tbr
//        @Test
        void removeLine_shouldThrowException_whenDataViolation() {
            // Arrange
            var orderId = UUID.randomUUID();
            var lineId = UUID.randomUUID();

            var customer = Instancio.create(Customer.class);
            var milk = Instancio.create(Milk.class);
            var order = MilkOrder.createMilkOrder(customer, "REF-123");
            var line = OrderLine.createOrderLine(milk, 2);
            ReflectionTestUtils.setField(line, "id", lineId);
            order.addOrderLine(line);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkOrderRepository.save(order)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.removeLine(orderId, lineId))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(milkOrderRepository).save(order);
        }
    }
}