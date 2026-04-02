package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.ConflictException;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.CustomerMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    CustomerMapper customerMapper;
    @InjectMocks
    CustomerServiceImpl customerService;

    // We use a model to ensure that email are valid in all tests
    private static final Model<CustomerRequest> REQUEST_MODEL = Instancio.of(CustomerRequest.class)
            .generate(field(CustomerRequest::email), gen -> gen.net().email())
            .toModel();

    private static final Model<CustomerPatchRequest> PATCH_MODEL = Instancio.of(CustomerPatchRequest.class)
            .generate(field(CustomerPatchRequest::email), gen -> gen.net().email())
            .toModel();

    // ---------------
    //    POSITIVE
    // ---------------

    @Nested
    class PositiveTests {

        @Test
        void create_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Instancio.create(Customer.class);
            var expectedResponse = Instancio.create(CustomerResponse.class);

            given(customerMapper.toEntity(request)).willReturn(customer);
            given(customerRepository.save(customer)).willReturn(customer);
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).save(customer);
        }

        @Test
        void getById_shouldReturnResponse_whenExists() {
            // Arrange
            var customer = Instancio.create(Customer.class);
            var expectedResponse = Instancio.create(CustomerResponse.class);

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.getById(customer.getId());

            // Assert
            assertThat(expectedResponse).isEqualTo(response);
            verify(customerRepository).findById(customer.getId());
        }

        @Test
        void list_shouldReturnPageOfResponse_whenExist() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var customer1 = Instancio.create(Customer.class);
            var customer2 = Instancio.create(Customer.class);
            var expectedResponse1 = Instancio.create(CustomerResponse.class);
            var expectedResponse2 = Instancio.create(CustomerResponse.class);
            var expectedPage = new PageImpl<>(List.of(customer1, customer2), pageable, 1);

            given(customerRepository.findAll(pageable)).willReturn(expectedPage);
            given(customerMapper.toResponse(customer1)).willReturn(expectedResponse1);
            given(customerMapper.toResponse(customer2)).willReturn(expectedResponse2);

            // Act
            var page = customerService.list(null, null, pageable);

            // Assert
            assertThat(page.getContent()).hasSize(2);
            verify(customerRepository).findAll(pageable);
        }

        @Test
        void update_shouldUpdateCustomer_whenExists() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Instancio.create(Customer.class);
            var expectedResponse = Instancio.create(CustomerResponse.class);

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.update(customer.getId(), request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customer.getId());
        }

        @Test
        void patch_shouldPatchCustomer_whenExists() {
            // Arrange
            var patch = Instancio.create(PATCH_MODEL);
            var customer = Instancio.create(Customer.class);
            var expectedResponse = Instancio.create(CustomerResponse.class);

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.patch(customer.getId(), patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customer.getId());
        }

        @Test
        void delete_shouldCallRepository_whenExists() {
            // Arrange
            var customer = Instancio.create(Customer.class);

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));

            // Act
            customerService.delete(customer.getId());

            // Assert
            verify(customerRepository).findById(customer.getId());
            verify(customerRepository).delete(customer);
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Test
        void create_shouldThrowException_whenDataViolation() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Instancio.create(Customer.class);

            given(customerMapper.toEntity(request)).willReturn(customer);
            given(customerRepository.save(customer)).willThrow(new DataIntegrityViolationException("Duplicated"));

            // Act + Assert
            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(DataIntegrityViolationException.class);

            verify(customerRepository).save(customer);
        }

        @Test
        void getById_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();

            given(customerRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.getById(id))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(id);
        }

        @Test
        void update_shouldThrowException_whenDataViolation() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Instancio.create(Customer.class);
            var existingCustomer = Instancio.create(Customer.class);

            ReflectionTestUtils.setField(customer, "email", "email@test.com");
            ReflectionTestUtils.setField(existingCustomer, "email", "email@test.com");

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(request.email())).willReturn(Optional.of(existingCustomer));

            // Act + Assert
            assertThatThrownBy(() -> customerService.update(customer.getId(), request))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void update_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);

            given(customerRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.update(id, request))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(id);
        }

        @Test
        void patch_shouldThrowException_whenDataViolation() {
            // Arrange
            var patch = Instancio.create(PATCH_MODEL);
            var customer = Instancio.create(Customer.class);
            var existingCustomer = Instancio.create(Customer.class);

            ReflectionTestUtils.setField(customer, "email", "email@test.com");
            ReflectionTestUtils.setField(existingCustomer, "email", "email@test.com");

            given(customerRepository.findById(customer.getId())).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(patch.email())).willReturn(Optional.of(existingCustomer));

            // Act + Assert
            assertThatThrownBy(() -> customerService.patch(customer.getId(), patch))
                    .isInstanceOf(ConflictException.class);
        }

        @Test
        void patch_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);

            given(customerRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.patch(id, patch))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(id);
        }

        @Test
        void delete_shouldThrowException_whenNotFound() {
            // Arrange
            var id = UUID.randomUUID();

            given(customerRepository.findById(id)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.delete(id))
                    .isInstanceOf(NotFoundException.class);

            verify(customerRepository).findById(id);
        }
    }
}
