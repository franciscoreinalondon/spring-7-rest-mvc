package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.dto.request.order.OrderShipmentRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = OrderShipmentMapperImpl.class)
class OrderShipmentMapperTest {

    @Autowired
    private OrderShipmentMapper mapper;

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Arrange
        var customer = Customer.createCustomer("John Doe", "john@test.com");
        var milkOrder = MilkOrder.createMilkOrder(customer, "ORDER-001");
        milkOrder.addOrderShipment("TRACK-001");

        var shipment = milkOrder.getOrderShipment();
        var request = new OrderShipmentRequest("TRACK-999");

        // Act
        mapper.updateEntity(shipment, request);

        // Assert
        assertThat(shipment.getTrackingNumber()).isEqualTo(request.trackingNumber());
    }

    @Test
    void updateEntity_shouldDoNothing_whenTargetIsNull() {
        // Arrange
        var request = new OrderShipmentRequest("TRACK-123");

        // Act + Assert
        assertDoesNotThrow(() -> mapper.updateEntity(null, request));
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestIsNull() {
        // Arrange
        var customer = Customer.createCustomer("John Doe", "john@test.com");
        var milkOrder = MilkOrder.createMilkOrder(customer, "ORDER-001");
        milkOrder.addOrderShipment("TRACK-001");

        var shipment = milkOrder.getOrderShipment();

        // Act
        mapper.updateEntity(shipment, null);

        // Assert
        assertThat(shipment.getTrackingNumber()).isEqualTo(milkOrder.getOrderShipment().getTrackingNumber());
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        var customer = Customer.createCustomer("John Doe", "john@test.com");
        var milkOrder = MilkOrder.createMilkOrder(customer, "ORDER-001");
        milkOrder.addOrderShipment("TRACK-001");

        var shipment = milkOrder.getOrderShipment();

        ReflectionTestUtils.setField(shipment, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(shipment, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));
        ReflectionTestUtils.setField(milkOrder, "id", UUID.randomUUID());

        // Act
        var response = mapper.toResponse(shipment);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(shipment.getId());
        assertThat(response.createdAt()).isEqualTo(shipment.getCreatedAt());
        assertThat(response.trackingNumber()).isEqualTo(shipment.getTrackingNumber());
        assertThat(response.milkOrderId()).isEqualTo(milkOrder.getId());
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}
