package com.franciscoreina.spring7.services;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
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

    Customer newCustomer;
    Customer savedCustomer;
    CustomerRequest createRequest;
    CustomerRequest updateRequest;
    CustomerPatchRequest patchRequest;
    CustomerResponse response;

    @BeforeEach
    void setUp() {
        newCustomer = TestDataFactory.getNewCustomer();
        savedCustomer = TestDataFactory.getSavedCustomer(newCustomer);
        createRequest = TestDataFactory.getCustomerCreateRequest(newCustomer);
        updateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);
        patchRequest = TestDataFactory.getCustomerPatchRequestWithName();
        response = TestDataFactory.getCustomerResponse(savedCustomer);
    }


    @Test
    void create_returnResponse_whenRequestValid() {
        // Arrange
        given(customerMapper.toEntity(createRequest)).willReturn(newCustomer);
        given(customerRepository.save(newCustomer)).willReturn(savedCustomer);
        given(customerMapper.toResponse(savedCustomer)).willReturn(response);

        // Act
        var customerResponse = customerService.create(createRequest);

        // Assert
        assertThat(customerResponse).isSameAs(this.response);

        verify(customerMapper).toEntity(createRequest);
        verify(customerRepository).save(newCustomer);
        verify(customerMapper).toResponse(savedCustomer);
    }

    @Test
    void create_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(customerMapper.toEntity(createRequest)).willReturn(newCustomer);
        given(customerRepository.save(newCustomer)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> customerService.create(createRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerMapper).toEntity(createRequest);
        verify(customerRepository).save(newCustomer);
    }

    @Test
    void getById_returnsResponse_whenCustomerExists() {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerRepository.findById(savedCustomerId)).willReturn(Optional.of(savedCustomer));
        given(customerMapper.toResponse(savedCustomer)).willReturn(response);

        // Act
        var customerResponse = customerService.getById(savedCustomerId);

        // Assert
        assertThat(customerResponse).isSameAs(this.response);

        verify(customerRepository).findById(savedCustomerId);
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

//    @Test
//    void list_returnsList_whenCustomersExist() {
//        // Arrange
//        var savedCustomer2 = TestDataFactory.getSavedCustomer(TestDataFactory.getNewCustomer());
//        given(customerRepository.findAll()).willReturn(List.of(savedCustomer, savedCustomer2));
//        given(customerMapper.toResponse(savedCustomer)).willReturn(TestDataFactory.getCustomerResponse(savedCustomer));
//        given(customerMapper.toResponse(savedCustomer2)).willReturn(TestDataFactory.getCustomerResponse(savedCustomer2));
//
//        // Act
//        var customerResponseList = customerService.list(, , );
//
//        // Assert
//        assertThat(customerResponseList).hasSize(2);
//        assertThat(customerResponseList.getFirst().email()).isEqualTo(savedCustomer.getEmail());
//        assertThat(customerResponseList.getLast().email()).isEqualTo(savedCustomer2.getEmail());
//
//        verify(customerRepository).findAll();
//        verify(customerMapper, times(1)).toResponse(savedCustomer);
//        verify(customerMapper, times(1)).toResponse(savedCustomer2);
//    }

//    @Test
//    void list_returnsEmptyList_whenNoCustomers() {
//        // Arrange
//        given(customerRepository.findAll()).willReturn(Collections.emptyList());
//
//        // Act
//        var customerResponseList = customerService.list(, , );
//
//        // Assert
//        assertThat(customerResponseList).isEmpty();
//
//        verify(customerRepository).findAll();
//        verifyNoInteractions(customerMapper);
//    }

    @Test
    void update_updatesEntity_whenCustomerExists() {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerRepository.findById(savedCustomerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.update(savedCustomerId, updateRequest);

        // Assert
        verify(customerRepository).findById(savedCustomerId);
        verify(customerMapper).updateEntity(savedCustomer, updateRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void update_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.update(UUID.randomUUID(), updateRequest))
                .isInstanceOf(NotFoundException.class);

        verifyNoInteractions(customerMapper);
    }

    @Test
    void update_propagatesDataIntegrityException_whenRepoRejects() {
        // Arrange
        given(customerRepository.findById(savedCustomer.getId())).willReturn(Optional.of(savedCustomer));
        given(customerRepository.save(savedCustomer)).willThrow(new DataIntegrityViolationException("Upc Duplicated"));

        // Act-Assert
        assertThatThrownBy(() -> customerService.update(savedCustomer.getId(), updateRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerRepository).findById(savedCustomer.getId());
        verify(customerMapper).updateEntity(savedCustomer, updateRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void patch_updatesOnlyProvidedFields_whenCustomerExists() {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerRepository.findById(savedCustomerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.patch(savedCustomerId, patchRequest);

        // Assert
        verify(customerRepository).findById(savedCustomerId);
        verify(customerMapper).patchEntity(savedCustomer, patchRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void patch_throwsNotFound_whenCustomerNotExists() {
        // Arrange
        given(customerRepository.findById(any(UUID.class))).willReturn(Optional.empty());

        // Act-Assert
        assertThatThrownBy(() -> customerService.patch(UUID.randomUUID(), patchRequest))
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
        assertThatThrownBy(() -> customerService.patch(savedCustomer.getId(), patchRequest))
                .isInstanceOf(DataIntegrityViolationException.class);

        verify(customerRepository).findById(savedCustomer.getId());
        verify(customerMapper).patchEntity(savedCustomer, patchRequest);
        verify(customerRepository).save(savedCustomer);
    }

    @Test
    void delete_deletesCustomer_whenCustomerExists() {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerRepository.findById(savedCustomerId)).willReturn(Optional.of(savedCustomer));

        // Act
        customerService.delete(savedCustomerId);

        // Assert
        verify(customerRepository).findById(savedCustomerId);
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
