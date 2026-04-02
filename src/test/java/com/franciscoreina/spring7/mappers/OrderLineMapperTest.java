package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.domain.order.OrderLineStatus;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrderLineMapperImpl.class)
class OrderLineMapperTest {

    @Autowired
    private OrderLineMapper mapper;

    @Test
    void toEntity_shouldCreateOrderLine() {
        // Arrange
        var category = Category.createCategory("Protein");
        var milk = Milk.createMilk(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(category)
        );

        var request = new OrderLineCreateRequest(5, UUID.randomUUID());

        // Act
        var orderLine = mapper.toEntity(request, milk);

        // Assert
        assertThat(orderLine).isNotNull();
        assertThat(orderLine.getRequestedQuantity()).isEqualTo(request.requestedQuantity());
        assertThat(orderLine.getAssignedQuantity()).isEqualTo(0);
        assertThat(orderLine.getOrderLineStatus()).isEqualTo(OrderLineStatus.NEW);
        assertThat(orderLine.getPriceAtPurchase()).isEqualTo(milk.getPrice());
        assertThat(orderLine.getMilkOrder()).isNull();
        assertThat(orderLine.getMilk()).isEqualTo(milk);
    }

    @Test
    void toEntity_shouldReturnNull_whenRequestIsNull() {
        // Arrange
        var category = Category.createCategory("Protein");
        var milk = Milk.createMilk(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(category)
        );

        // Act
        var result = mapper.toEntity(null, milk);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toEntity_shouldReturnNull_whenMilkIsNull() {
        // Arrange
        var request = new OrderLineCreateRequest(5, UUID.randomUUID());

        // Act
        var result = mapper.toEntity(request, null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        var category = Category.createCategory("Protein");
        var milk = Milk.createMilk(
                "Whole Milk",
                MilkType.WHOLE,
                "UPC123",
                new BigDecimal("2.50"),
                10,
                Set.of(category)
        );

        var customer = Customer.createCustomer("John Doe", "john@test.com");
        var milkOrder = MilkOrder.createMilkOrder(customer, "ORDER-001");
        var orderLine = OrderLine.createOrderLine(milk, 5);
        milkOrder.addOrderLine(orderLine);

        ReflectionTestUtils.setField(orderLine, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(orderLine, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));
        ReflectionTestUtils.setField(orderLine, "updatedAt", Instant.parse("2026-04-01T10:00:00Z"));
        ReflectionTestUtils.setField(milkOrder, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(milk, "id", UUID.randomUUID());

        // Act
        var response = mapper.toResponse(orderLine);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(orderLine.getId());
        assertThat(response.createdAt()).isEqualTo(orderLine.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(orderLine.getUpdatedAt());
        assertThat(response.requestedQuantity()).isEqualTo(orderLine.getRequestedQuantity());
        assertThat(response.assignedQuantity()).isEqualTo(orderLine.getAssignedQuantity());
        assertThat(response.orderLineStatus()).isEqualTo(orderLine.getOrderLineStatus());
        assertThat(response.priceAtPurchase()).isEqualTo(orderLine.getPriceAtPurchase());
        assertThat(response.milkOrderId()).isEqualTo(milkOrder.getId());
        assertThat(response.milkId()).isEqualTo(milk.getId());
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}