package com.franciscoreina.spring7.mappers;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
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
@ContextConfiguration(classes = CustomerMapperImpl.class)
class CustomerMapperTest {

    @Autowired
    private CustomerMapper mapper;

    @Test
    void updateEntity_shouldUpdateAllFields() {
        // Arrange
        var customer = Customer.createCustomer("Old Name", "old@test.com");
        var request = new CustomerRequest("New Name", "new@test.com");

        // Act
        mapper.updateEntity(customer, request);

        // Assert
        assertThat(customer.getName()).isEqualTo(request.name());
        assertThat(customer.getEmail()).isEqualTo(request.email());
    }

    @Test
    void updateEntity_shouldDoNothing_whenTargetIsNull() {
        // Arrange
        var request = new CustomerRequest("New Name", "new@test.com");

        // Act + Assert
        assertDoesNotThrow(() -> mapper.updateEntity(null, request));
    }

    @Test
    void updateEntity_shouldDoNothing_whenRequestIsNull() {
        // Arrange
        var customer = Customer.createCustomer("Old Name", "old@test.com");

        // Act + Assert
        assertDoesNotThrow(() -> mapper.updateEntity(customer, null));
    }

    @Test
    void patchEntity_shouldUpdateOnlyNonNullFields() {
        // Arrange
        var customer = Customer.createCustomer("Old Name", "old@test.com");
        var patch = new CustomerPatchRequest("New Name", null);

        // Act
        mapper.patchEntity(customer, patch);

        // Assert
        assertThat(customer.getName()).isEqualTo(patch.name());
        assertThat(customer.getEmail()).isEqualTo("old@test.com"); // unchanged
    }

    @Test
    void toResponse_shouldMapAllFields() {
        // Arrange
        var customer = Customer.createCustomer("John", "john@test.com");

        ReflectionTestUtils.setField(customer, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(customer, "createdAt", Instant.parse("2026-04-01T10:00:00Z"));

        // Act
        var response = mapper.toResponse(customer);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(customer.getId());
        assertThat(response.createdAt()).isEqualTo(customer.getCreatedAt());
        assertThat(response.name()).isEqualTo(customer.getName());
        assertThat(response.email()).isEqualTo(customer.getEmail());
    }

    @Test
    void toResponse_shouldReturnNull_whenInputIsNull() {
        // Act
        var response = mapper.toResponse(null);

        // Assert
        assertThat(response).isNull();
    }
}