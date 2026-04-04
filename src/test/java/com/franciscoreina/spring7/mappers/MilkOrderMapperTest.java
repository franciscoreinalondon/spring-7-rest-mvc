package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
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
@ContextConfiguration(classes = {MilkOrderMapperImpl.class, OrderLineMapperImpl.class})
class MilkOrderMapperTest {

    @Autowired
    private MilkOrderMapper mapper;

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
        milkOrder.addOrderShipment("TRACK-001");

        ReflectionTestUtils.setField(milk, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());

        ReflectionTestUtils.setField(orderLine, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(milkOrder, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(milkOrder, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));
        ReflectionTestUtils.setField(milkOrder, "updatedAt", Instant.parse("2026-04-01T11:00:00Z"));
        ReflectionTestUtils.setField(milkOrder.getOrderShipment(), "id", UUID.randomUUID());

        // Act
        var response = mapper.toResponse(milkOrder);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(milkOrder.getId());
        assertThat(response.createdAt()).isEqualTo(milkOrder.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(milkOrder.getUpdatedAt());
        assertThat(response.customerRef()).isEqualTo(milkOrder.getCustomerRef());
        assertThat(response.paymentAmount()).isEqualTo(milkOrder.getPaymentAmount());
        assertThat(response.milkOrderStatus()).isEqualTo(milkOrder.getMilkOrderStatus());
        assertThat(response.customerId()).isEqualTo(customer.getId());
        assertThat(response.orderShipmentId()).isEqualTo(milkOrder.getOrderShipment().getId());

        assertThat(response.orderLines()).hasSize(1);
        var responseOrderLine = response.orderLines().iterator().next();
        assertThat(responseOrderLine.id()).isEqualTo(orderLine.getId());
        assertThat(responseOrderLine.requestedQuantity()).isEqualTo(orderLine.getRequestedQuantity());
        assertThat(responseOrderLine.assignedQuantity()).isEqualTo(orderLine.getAssignedQuantity());
        assertThat(responseOrderLine.orderLineStatus()).isEqualTo(orderLine.getOrderLineStatus());
        assertThat(responseOrderLine.priceAtPurchase()).isEqualTo(orderLine.getPriceAtPurchase());
        assertThat(responseOrderLine.milkOrderId()).isEqualTo(milkOrder.getId());
        assertThat(responseOrderLine.milkId()).isEqualTo(milk.getId());
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}