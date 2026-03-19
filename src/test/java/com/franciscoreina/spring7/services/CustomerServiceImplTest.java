package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.dto.request.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.mappers.CustomerMapper;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    CustomerRepository customerRepository;

    @Mock
    CustomerMapper customerMapper;

    @InjectMocks
    CustomerServiceImpl customerService;

    Customer customer;
    Customer savedCustomer;
    CustomerCreateRequest customerCreateRequest;
    CustomerUpdateRequest customerUpdateRequest;
    CustomerPatchRequest customerPatchRequest;
    CustomerResponse customerResponse;

    @BeforeEach
    void setUp() {
        customer = TestDataFactory.newCustomer();
        savedCustomer = TestDataFactory.newSavedCustomer(customer);
        customerCreateRequest = TestDataFactory.newCustomerCreateRequest(customer);
        customerUpdateRequest = TestDataFactory.newCustomerUpdateRequest(savedCustomer);
        customerPatchRequest = TestDataFactory.newCustomerPatchRequestWithName();
        customerResponse = TestDataFactory.newCustomerResponse(savedCustomer);
    }


    @Test
    void create_returnResponse_whenRequestValid() {
        // Arrange
        given(customerMapper.toEntity(customerCreateRequest)).willReturn(customer);
        given(customerRepository.save(customer)).willReturn(savedCustomer);
        given(customerMapper.toResponse(savedCustomer)).willReturn(customerResponse);

        // Act
        CustomerResponse customerResponse = customerService.create(customerCreateRequest);

        // Assert
        assertThat(customerResponse).isSameAs(this.customerResponse);

        verify(customerMapper).toEntity(customerCreateRequest);
        verify(customerRepository).save(customer);
        verify(customerMapper).toResponse(savedCustomer);
    }

    @Test
    void create_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(customerMapper.toEntity(customerCreateRequest)).willReturn(customer);
        given(customerRepository.save(customer)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> customerService.create(customerCreateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerMapper).toEntity(customerCreateRequest);
        verify(customerRepository).save(customer);
    }

    @Test
    void getById_returnsResponse_whenCustomerExists() {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(savedCustomer));
        given(customerMapper.toResponse(savedCustomer)).willReturn(customerResponse);

        // Act
        CustomerResponse customerResponse = customerService.getById(customerId);

        // Assert
        assertThat(customerResponse).isSameAs(this.customerResponse);

        verify(customerRepository).findById(customerId);
        verify(customerMapper).toResponse(savedCustomer);
    }

    @Test
    void getById_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.getById(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);

        verify(customerRepository).findById(any(UUID.class));
        verifyNoInteractions(customerMapper);
    }

    @Test
    void list_returnsList_whenCustomersExist() {
        // Arrange
        Customer savedCustomer2 = TestDataFactory.newSavedCustomer(TestDataFactory.newCustomer());
        given(customerRepository.findAll()).willReturn(List.of(savedCustomer, savedCustomer2));
        given(customerMapper.toResponse(savedCustomer)).willReturn(TestDataFactory.newCustomerResponse(savedCustomer));
        given(customerMapper.toResponse(savedCustomer2)).willReturn(TestDataFactory.newCustomerResponse(savedCustomer2));

        // Act
        List<CustomerResponse> customerResponseList = customerService.list();

        // Assert
        assertThat(customerResponseList).hasSize(2);
        assertThat(customerResponseList.getFirst().email()).isEqualTo(savedCustomer.getEmail());
        assertThat(customerResponseList.getLast().email()).isEqualTo(savedCustomer2.getEmail());

        verify(customerRepository).findAll();
        verify(customerMapper, times(1)).toResponse(savedCustomer);
        verify(customerMapper, times(1)).toResponse(savedCustomer2);
    }

    @Test
    void list_returnsEmptyList_whenNoCustomers() {
        // Arrange
        given(customerRepository.findAll()).willReturn(Collections.emptyList());

        // Act
        List<CustomerResponse> customerResponseList = customerService.list();

        // Assert
        assertThat(customerResponseList).isEmpty();

        verify(customerRepository).findAll();
        verifyNoInteractions(customerMapper);
    }

    @Test
    void update_updatesEntity_whenCustomerExists() {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.update(customerId, customerUpdateRequest);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(customerMapper).updateEntity(savedCustomer, customerUpdateRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void update_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.update(UUID.randomUUID(), customerUpdateRequest))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(customerMapper);
    }

    @Test
    void update_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(customerRepository.findById(savedCustomer.getId())).willReturn(Optional.of(savedCustomer));
        given(customerRepository.save(savedCustomer)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> customerService.update(savedCustomer.getId(), customerUpdateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerRepository).findById(savedCustomer.getId());
        verify(customerMapper).updateEntity(savedCustomer, customerUpdateRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void patch_updatesOnlyProvidedFields_whenCustomerExists() {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.patch(customerId, customerPatchRequest);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(customerMapper).patchEntity(savedCustomer, customerPatchRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void patch_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.patch(UUID.randomUUID(), customerPatchRequest))
                .isInstanceOf(NotFoundException.class);

        verify(customerRepository).findById(any(UUID.class));
        verifyNoInteractions(customerMapper);
    }

    @Test
    void patch_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(customerRepository.findById(savedCustomer.getId())).willReturn(Optional.of(savedCustomer));
        given(customerRepository.save(savedCustomer)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> customerService.patch(savedCustomer.getId(), customerPatchRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerRepository).findById(savedCustomer.getId());
        verify(customerMapper).patchEntity(savedCustomer, customerPatchRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void delete_deletesCustomer_whenCustomerExists() {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerRepository.findById(customerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.delete(customerId);

        // Assert
        verify(customerRepository).findById(customerId);
        verify(customerRepository).delete(savedCustomer);
    }

    @Test
    void delete_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.delete(UUID.randomUUID()))
                .isInstanceOf(NotFoundException.class);

        verify(customerRepository).findById(any(UUID.class));
    }
}
