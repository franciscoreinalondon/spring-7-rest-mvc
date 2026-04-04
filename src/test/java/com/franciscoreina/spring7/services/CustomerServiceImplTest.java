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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private CustomerMapper customerMapper;
    @InjectMocks
    private CustomerServiceImpl customerService;

    private static final Model<CustomerRequest> REQUEST_MODEL = Instancio.of(CustomerRequest.class)
            .set(field(CustomerRequest::name), "Request name")
            .set(field(CustomerRequest::email), "request@test.com")
            .toModel();

    private static final Model<CustomerPatchRequest> PATCH_MODEL = Instancio.of(CustomerPatchRequest.class)
            .set(field(CustomerPatchRequest::name), "Patch name")
            .set(field(CustomerPatchRequest::email), "patch@test.com")
            .toModel();

    @Nested
    class CreateTests {

        @Test
        void create_shouldReturnResponse_whenRequestIsValid() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var savedCustomer = Customer.createCustomer(request.name(), request.email());
            var expectedResponse = Instancio.create(CustomerResponse.class);
            var customerCaptor = ArgumentCaptor.forClass(Customer.class);

            given(customerRepository.existsByEmailIgnoreCase(request.email())).willReturn(false);
            given(customerRepository.save(any(Customer.class))).willReturn(savedCustomer);
            given(customerMapper.toResponse(savedCustomer)).willReturn(expectedResponse);

            // Act
            var response = customerService.create(request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);

            verify(customerRepository).existsByEmailIgnoreCase(request.email());
            verify(customerRepository).save(customerCaptor.capture());
            verify(customerMapper).toResponse(savedCustomer);

            var capturedCustomer = customerCaptor.getValue();
            assertThat(capturedCustomer.getName()).isEqualTo(request.name());
            assertThat(capturedCustomer.getEmail()).isEqualTo(request.email());
        }

        @Test
        void create_shouldThrowConflictException_whenEmailAlreadyExists() {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);

            given(customerRepository.existsByEmailIgnoreCase(request.email())).willReturn(true);

            // Act + Assert
            assertThatThrownBy(() -> customerService.create(request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Customer email already exists: " + request.email());

            verify(customerRepository).existsByEmailIgnoreCase(request.email());
            verifyNoInteractions(customerMapper);
            verify(customerRepository, never()).save(any());
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnResponse_whenCustomerExists() {
            // Arrange
            var customerId = UUID.randomUUID();
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.getById(customerId);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customerId);
            verify(customerMapper).toResponse(customer);
        }

        @Test
        void getById_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
            // Arrange
            var customerId = UUID.randomUUID();

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.getById(customerId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verify(customerRepository).findById(customerId);
            verify(customerMapper, never()).toResponse(any());
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnAllCustomers_whenNoFiltersAreProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var customer1 = Customer.createCustomer("John Doe", "john@test.com");
            var customer2 = Customer.createCustomer("Jane Doe", "jane@test.com");
            var response1 = Instancio.create(CustomerResponse.class);
            var response2 = Instancio.create(CustomerResponse.class);
            var page = new PageImpl<>(List.of(customer1, customer2), pageable, 2);

            given(customerRepository.findAll(pageable)).willReturn(page);
            given(customerMapper.toResponse(customer1)).willReturn(response1);
            given(customerMapper.toResponse(customer2)).willReturn(response2);

            // Act
            var result = customerService.search(null, null, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response1, response2);
            verify(customerRepository).findAll(pageable);
        }

        @Test
        void search_shouldSearchByName_whenOnlyNameIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var response = Instancio.create(CustomerResponse.class);
            var page = new PageImpl<>(List.of(customer), pageable, 1);

            given(customerRepository.findAllByNameContainingIgnoreCase(customer.getName(), pageable)).willReturn(page);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // Act
            var result = customerService.search(" John Doe ", null, pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(customerRepository).findAllByNameContainingIgnoreCase(customer.getName(), pageable);
        }

        @Test
        void search_shouldSearchByEmail_whenOnlyEmailIsProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var response = Instancio.create(CustomerResponse.class);
            var page = new PageImpl<>(List.of(customer), pageable, 1);

            given(customerRepository.findAllByEmailIgnoreCase(customer.getEmail(), pageable)).willReturn(page);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // Act
            var result = customerService.search(null, " john@test.com ", pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(customerRepository).findAllByEmailIgnoreCase(customer.getEmail(), pageable);
        }

        @Test
        void search_shouldSearchByNameAndEmail_whenBothFiltersAreProvided() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var response = Instancio.create(CustomerResponse.class);
            var page = new PageImpl<>(List.of(customer), pageable, 1);

            given(customerRepository.findAllByNameContainingIgnoreCaseAndEmailIgnoreCase(customer.getName(), customer.getEmail(), pageable))
                    .willReturn(page);
            given(customerMapper.toResponse(customer)).willReturn(response);

            // Act
            var result = customerService.search(" John Doe ", " john@test.com ", pageable);

            // Assert
            assertThat(result.getContent()).containsExactly(response);
            verify(customerRepository).findAllByNameContainingIgnoreCaseAndEmailIgnoreCase(customer.getName(), customer.getEmail(), pageable);
        }

        @Test
        void search_shouldTreatBlankFiltersAsNull() {
            // Arrange
            var pageable = Pageable.ofSize(10);
            var page = new PageImpl<Customer>(List.of(), pageable, 0);

            given(customerRepository.findAll(pageable)).willReturn(page);

            // Act
            var result = customerService.search(" ", " ", pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            verify(customerRepository).findAll(pageable);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_shouldReturnUpdatedResponse_whenRequestIsValid() {
            // Arrange
            var customerId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Customer.createCustomer("Old Name", "old@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(request.email())).willReturn(Optional.empty());
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.update(customerId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customerId);
            verify(customerRepository).findByEmailIgnoreCase(request.email());
            verify(customerMapper).updateEntity(customer, request);
            verify(customerMapper).toResponse(customer);
            verify(customerRepository, never()).save(any());
        }

        @Test
        void update_shouldNotCheckEmailUniqueness_whenEmailDoesNotChange() {
            // Arrange
            var customerId = UUID.randomUUID();
            var request = new CustomerRequest("New Name", "same@test.com");
            var customer = Customer.createCustomer("Old Name", "same@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.update(customerId, request);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerMapper).updateEntity(customer, request);
            verify(customerRepository, never()).findByEmailIgnoreCase(any());
        }

        @Test
        void update_shouldThrowConflictException_whenEmailBelongsToAnotherCustomer() {
            // Arrange
            var customerId = UUID.randomUUID();
            var existingCustomerId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var existingCustomer = Customer.createCustomer("Jane Doe", request.email());

            ReflectionTestUtils.setField(customer, "id", customerId);
            ReflectionTestUtils.setField(existingCustomer, "id", existingCustomerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(request.email())).willReturn(Optional.of(existingCustomer));

            // Act + Assert
            assertThatThrownBy(() -> customerService.update(customerId, request))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Customer email already exists: " + request.email());

            verify(customerRepository).findById(customerId);
            verify(customerRepository).findByEmailIgnoreCase(request.email());
            verify(customerMapper, never()).updateEntity(any(), any());
        }

        @Test
        void update_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
            // Arrange
            var customerId = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.update(customerId, request))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verify(customerRepository).findById(customerId);
            verifyNoMoreInteractions(customerRepository);
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_shouldReturnPatchedResponse_whenRequestContainsChanges() {
            // Arrange
            var customerId = UUID.randomUUID();
            var patch = new CustomerPatchRequest("New Name", "new@test.com");
            var customer = Customer.createCustomer("Old Name", "old@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(patch.email())).willReturn(Optional.empty());
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.patch(customerId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customerId);
            verify(customerRepository).findByEmailIgnoreCase(patch.email());
            verify(customerMapper).patchEntity(customer, patch);
            verify(customerMapper).toResponse(customer);
            verify(customerRepository, never()).save(any());
        }

        @Test
        void patch_shouldReturnCurrentResponse_whenPatchIsEmpty() {
            // Arrange
            var customerId = UUID.randomUUID();
            var patch = new CustomerPatchRequest(null, null);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.patch(customerId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository).findById(customerId);
            verify(customerMapper).toResponse(customer);
            verify(customerRepository, never()).findByEmailIgnoreCase(any());
            verify(customerMapper, never()).patchEntity(any(), any());
        }

        @Test
        void patch_shouldNotCheckEmailUniqueness_whenEmailIsNull() {
            // Arrange
            var customerId = UUID.randomUUID();
            var patch = new CustomerPatchRequest("New Name", null);
            var customer = Customer.createCustomer("Old Name", "old@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.patch(customerId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerRepository, never()).findByEmailIgnoreCase(any());
            verify(customerMapper).patchEntity(customer, patch);
            verify(customerMapper).toResponse(customer);
        }

        @Test
        void patch_shouldNotCheckEmailUniqueness_whenEmailDoesNotChange() {
            // Arrange
            var customerId = UUID.randomUUID();
            var customer = Customer.createCustomer("New name", "same@test.com");
            var patch = new CustomerPatchRequest("Old name", "same@test.com");
            var expectedResponse = Instancio.create(CustomerResponse.class);

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerMapper.toResponse(customer)).willReturn(expectedResponse);

            // Act
            var response = customerService.patch(customerId, patch);

            // Assert
            assertThat(response).isEqualTo(expectedResponse);
            verify(customerMapper).patchEntity(customer, patch);
            verify(customerRepository, never()).findByEmailIgnoreCase(any());
            verify(customerMapper).toResponse(customer);

        }

        @Test
        void patch_shouldThrowConflictException_whenEmailBelongsToAnotherCustomer() {
            // Arrange
            var customerId = UUID.randomUUID();
            var existingCustomerId = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);
            var customer = Customer.createCustomer("John Doe", "john@test.com");
            var existingCustomer = Customer.createCustomer("Jane Doe", patch.email());

            ReflectionTestUtils.setField(customer, "id", customerId);
            ReflectionTestUtils.setField(existingCustomer, "id", existingCustomerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));
            given(customerRepository.findByEmailIgnoreCase(patch.email())).willReturn(Optional.of(existingCustomer));

            // Act + Assert
            assertThatThrownBy(() -> customerService.patch(customerId, patch))
                    .isInstanceOf(ConflictException.class)
                    .hasMessage("Customer email already exists: " + patch.email());

            verify(customerRepository).findById(customerId);
            verify(customerRepository).findByEmailIgnoreCase(patch.email());
            verify(customerMapper, never()).patchEntity(any(), any());
        }

        @Test
        void patch_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
            // Arrange
            var customerId = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.patch(customerId, patch))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verify(customerRepository).findById(customerId);
            verifyNoMoreInteractions(customerRepository);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void delete_shouldCallRepository_whenCustomerExists() {
            // Arrange
            var customerId = UUID.randomUUID();
            var customer = Customer.createCustomer("John Doe", "john@test.com");

            ReflectionTestUtils.setField(customer, "id", customerId);

            given(customerRepository.findById(customerId)).willReturn(Optional.of(customer));

            // Act
            customerService.delete(customerId);

            // Assert
            verify(customerRepository).findById(customerId);
            verify(customerRepository).delete(customer);
        }

        @Test
        void delete_shouldThrowNotFoundException_whenCustomerDoesNotExist() {
            // Arrange
            var customerId = UUID.randomUUID();

            given(customerRepository.findById(customerId)).willReturn(Optional.empty());

            // Act + Assert
            assertThatThrownBy(() -> customerService.delete(customerId))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage("Customer not found: " + customerId);

            verify(customerRepository).findById(customerId);
            verify(customerRepository, never()).delete(any());
        }
    }
}
