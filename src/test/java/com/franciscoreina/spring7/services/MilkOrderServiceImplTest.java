package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MilkOrderServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private MilkRepository milkRepository;
    @Mock
    private MilkOrderRepository milkOrderRepository;
    @Mock
    private MilkOrderMapper milkOrderMapper;
    @Mock
    private OrderLineMapper orderLineMapper;
    @InjectMocks
    private MilkOrderServiceImpl milkOrderService;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();
    private static final UUID MILK_ID_1 = UUID.randomUUID();
    private static final UUID MILK_ID_2 = UUID.randomUUID();

    private static final OrderLineCreateRequest ORDER_LINE_1 = new OrderLineCreateRequest(2, MILK_ID_1);
    private static final OrderLineCreateRequest ORDER_LINE_2 = new OrderLineCreateRequest(4, MILK_ID_2);

    private static final Model<OrderLineCreateRequest> ORDER_LINE_CREATE_REQUEST_MODEL =
            Instancio.of(OrderLineCreateRequest.class)
                    .set(field(OrderLineCreateRequest::requestedQuantity), 3)
                    .set(field(OrderLineCreateRequest::milkId), MILK_ID_1)
                    .toModel();

    private static final Model<OrderLineUpdateRequest> ORDER_LINE_UPDATE_REQUEST_MODEL =
            Instancio.of(OrderLineUpdateRequest.class)
                    .set(field(OrderLineUpdateRequest::requestedQuantity), 5)
                    .toModel();

    private static final Model<MilkOrderRequest> MILK_ORDER_REQUEST_MODEL =
            Instancio.of(MilkOrderRequest.class)
                    .set(field(MilkOrderRequest::customerRef), "ORDER-123")
                    .set(field(MilkOrderRequest::customerId), CUSTOMER_ID)
                    .set(field(MilkOrderRequest::orderLines), new LinkedHashSet<>(Arrays.asList(ORDER_LINE_1, ORDER_LINE_2)))
                    .toModel();

    @Nested
    class CreateTests {

        @Test
        void create_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var request = Instancio.create(MILK_ORDER_REQUEST_MODEL);

            var customer = customer();
            var milk1 = milk("Milk 1", "UPC111");
            var milk2 = milk("Milk 2", "UPC222");
            var order = MilkOrder.createMilkOrder(customer, request.customerRef());
            var line1 = OrderLine.createOrderLine(milk1, ORDER_LINE_1.requestedQuantity());
            var line2 = OrderLine.createOrderLine(milk2, ORDER_LINE_2.requestedQuantity());
            var expectedResponse = Instancio.create(MilkOrderResponse.class);

            given(customerRepository.findById(request.customerId())).willReturn(Optional.of(customer));
            given(milkOrderMapper.toEntity(request, customer)).willReturn(order);
            given(milkRepository.findById(MILK_ID_1)).willReturn(Optional.of(milk1));
            given(milkRepository.findById(MILK_ID_2)).willReturn(Optional.of(milk2));
            given(orderLineMapper.toEntity(ORDER_LINE_1, milk1)).willReturn(line1);
            given(orderLineMapper.toEntity(ORDER_LINE_2, milk2)).willReturn(line2);
            given(milkOrderRepository.save(order)).willReturn(order);
            given(milkOrderMapper.toResponse(order)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(request.customerId());
            verify(milkOrderMapper).toEntity(request, customer);
            verify(milkRepository).findById(MILK_ID_1);
            verify(milkRepository).findById(MILK_ID_2);
            verify(orderLineMapper).toEntity(ORDER_LINE_1, milk1);
            verify(orderLineMapper).toEntity(ORDER_LINE_2, milk2);
            verify(milkOrderRepository).save(order);
            verify(milkOrderMapper).toResponse(order);
        }

        @Test
        void create_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
            // Arrange
            var request = Instancio.create(MILK_ORDER_REQUEST_MODEL);

            given(customerRepository.findById(request.customerId())).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Customer not found: " + request.customerId());

            verify(customerRepository).findById(request.customerId());
            verifyNoInteractions(milkRepository, milkOrderRepository, milkOrderMapper, orderLineMapper);
        }

        @Test
        void create_shouldThrowNotFoundException_whenAnyMilkDoesNotExist() {
            // Arrange
            var request = Instancio.create(MILK_ORDER_REQUEST_MODEL);

            var customer = customer();
            var order = MilkOrder.createMilkOrder(customer, request.customerRef());
            var milk1 = milk("Milk 1", "UPC111");
            var line1 = OrderLine.createOrderLine(milk1, ORDER_LINE_1.requestedQuantity());

            given(customerRepository.findById(request.customerId())).willReturn(Optional.of(customer));
            given(milkOrderMapper.toEntity(request, customer)).willReturn(order);
            given(milkRepository.findById(MILK_ID_1)).willReturn(Optional.of(milk1));
            given(milkRepository.findById(MILK_ID_2)).willReturn(Optional.empty());
            given(orderLineMapper.toEntity(ORDER_LINE_1, milk1)).willReturn(line1);

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.create(request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + MILK_ID_2);

            verify(customerRepository).findById(request.customerId());
            verify(milkOrderMapper).toEntity(request, customer);
            verify(milkRepository).findById(MILK_ID_1);
            verify(milkRepository).findById(MILK_ID_2);
            verify(milkOrderRepository, never()).save(any());
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnResponse_whenMilkOrderExists() {
            // Arrange
            var orderId = UUID.randomUUID();
            var order = Instancio.create(MilkOrder.class);
            var expectedResponse = Instancio.create(MilkOrderResponse.class);

            ReflectionTestUtils.setField(order, "id", orderId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkOrderMapper.toResponse(order)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.getById(orderId);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).findById(orderId);
            verify(milkOrderMapper).toResponse(order);
        }

        @Test
        void getById_shouldThrowNotFoundException_whenMilkOrderDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.getById(orderId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk order not found: " + orderId);

            verify(milkOrderRepository).findById(orderId);
            verify(milkOrderMapper, never()).toResponse(any());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnAllOrders_whenNoFilterIsProvided() {
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
            var result = milkOrderService.search(null, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response1, response2);
            verify(milkOrderRepository).findAll(pageable);
        }

        @Test
        void search_shouldSearchByCustomerRef_whenFilterIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var order = Instancio.create(MilkOrder.class);
            var response = Instancio.create(MilkOrderResponse.class);
            var page = new PageImpl<>(List.of(order), pageable, 1);

            given(milkOrderRepository.findAllByCustomerRefContainingIgnoreCase("ORDER-123", pageable)).willReturn(page);
            given(milkOrderMapper.toResponse(order)).willReturn(response);

            // Act
            var result = milkOrderService.search(" ORDER-123 ", pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(milkOrderRepository).findAllByCustomerRefContainingIgnoreCase("ORDER-123", pageable);
        }

        @Test
        void search_shouldTreatBlankFilterAsNull() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<MilkOrder>(List.of(), pageable, 0);

            given(milkOrderRepository.findAll(pageable)).willReturn(page);

            // Act
            var result = milkOrderService.search(" ", pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(milkOrderRepository).findAll(pageable);
        }
    }

    @Nested
    class AddLineTests {

        @Test
        void addLine_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var orderId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_CREATE_REQUEST_MODEL);

            var order = order();
            var milk = milk("Milk", "UPC123");
            var newLine = OrderLine.createOrderLine(milk, request.requestedQuantity());
            var expectedResponse = Instancio.create(OrderLineResponse.class);

            ReflectionTestUtils.setField(order, "id", orderId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkRepository.findById(request.milkId())).willReturn(Optional.of(milk));
            given(orderLineMapper.toEntity(request, milk)).willReturn(newLine);
            given(orderLineMapper.toResponse(newLine)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.addLine(orderId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).findById(orderId);
            verify(milkRepository).findById(request.milkId());
            verify(orderLineMapper).toEntity(request, milk);
            verify(orderLineMapper).toResponse(newLine);
        }

        @Test
        void addLine_shouldThrowNotFoundException_whenMilkOrderDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_CREATE_REQUEST_MODEL);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.addLine(orderId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk order not found: " + orderId);

            verify(milkOrderRepository).findById(orderId);
            verifyNoInteractions(milkRepository, orderLineMapper);
        }

        @Test
        void addLine_shouldThrowNotFoundException_whenMilkDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_CREATE_REQUEST_MODEL);
            var order = order();

            ReflectionTestUtils.setField(order, "id", orderId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(milkRepository.findById(request.milkId())).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.addLine(orderId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk not found: " + request.milkId());

            verify(milkOrderRepository).findById(orderId);
            verify(milkRepository).findById(request.milkId());
            verify(orderLineMapper, never()).toEntity(any(), any());
        }
    }

    @Nested
    class UpdateLineTests {

        @Test
        void updateLine_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_UPDATE_REQUEST_MODEL);

            var milk = milk("Milk", "UPC123");
            var order = order();
            var line = OrderLine.createOrderLine(milk, ORDER_LINE_1.requestedQuantity());
            order.addOrderLine(line);

            var expectedResponse = Instancio.create(OrderLineResponse.class);

            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(line, "id", orderLineId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(orderLineMapper.toResponse(line)).willReturn(expectedResponse);

            // Act
            var response = milkOrderService.updateLine(orderId, orderLineId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(milkOrderRepository).findById(orderId);
            verify(orderLineMapper).toResponse(line);
        }

        @Test
        void updateLine_shouldThrowNotFoundException_whenMilkOrderDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_UPDATE_REQUEST_MODEL);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.updateLine(orderId, orderLineId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk order not found: " + orderId);

            verify(milkOrderRepository).findById(orderId);
            verify(orderLineMapper, never()).toResponse(any());
        }

        @Test
        void updateLine_shouldThrowNotFoundException_whenOrderLineDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            var request = Instancio.create(ORDER_LINE_UPDATE_REQUEST_MODEL);
            var order = order();

            ReflectionTestUtils.setField(order, "id", orderId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.updateLine(orderId, orderLineId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order line not found: " + orderLineId);

            verify(milkOrderRepository).findById(orderId);
            verify(orderLineMapper, never()).toResponse(any());
        }
    }

    @Nested
    class RemoveLineTests {

        @Test
        void removeLine_shouldRemoveOrderLine_whenOrderAndLineExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            var milk = milk("Milk", "UPC123");
            var order = order();
            var line = OrderLine.createOrderLine(milk, 2);
            order.addOrderLine(line);

            ReflectionTestUtils.setField(order, "id", orderId);
            ReflectionTestUtils.setField(line, "id", orderLineId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));

            // Act
            milkOrderService.removeLine(orderId, orderLineId);

            // Assert
            verify(milkOrderRepository).findById(orderId);
            assertThat(order.getOrderLines()).isEmpty();
        }

        @Test
        void removeLine_shouldThrowNotFoundException_whenMilkOrderDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            given(milkOrderRepository.findById(orderId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.removeLine(orderId, orderLineId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Milk order not found: " + orderId);

            verify(milkOrderRepository).findById(orderId);
        }

        @Test
        void removeLine_shouldThrowNotFoundException_whenOrderLineDoesNotExist() {
            // Arrange
            var orderId = UUID.randomUUID();
            var orderLineId = UUID.randomUUID();
            var order = order();

            ReflectionTestUtils.setField(order, "id", orderId);

            given(milkOrderRepository.findById(orderId)).willReturn(Optional.of(order));

            // Act + Assert
            assertThatThrownBy(() -> milkOrderService.removeLine(orderId, orderLineId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Order line not found: " + orderLineId);

            verify(milkOrderRepository).findById(orderId);
        }
    }

    private Customer customer() {
        return Customer.createCustomer("John Doe", "john@test.com");
    }

    private Milk milk(String name, String upc) {
        return Milk.createMilk(
                name,
                MilkType.WHOLE,
                upc,
                new BigDecimal("2.50"),
                10,
                Set.of(Category.createCategory("Category"))
        );
    }

    private MilkOrder order() {
        return MilkOrder.createMilkOrder(customer(), "ORDER-123");
    }
}
