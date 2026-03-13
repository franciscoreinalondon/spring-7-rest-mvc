package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.CustomerService;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ImportAutoConfiguration(exclude = OAuth2ResourceServerAutoConfiguration.class)
@WebMvcTest(CustomerController.class)
public class CustomerControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    CustomerService customerService;

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
    void postCustomer_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(customerService.create(customerCreateRequest)).willReturn(customerResponse);

        // Act
        mockMvc.perform(post(ApiPaths.CUSTOMERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(customerCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.CUSTOMERS + "/" + customerResponse.id()));

        // Assert
        verify(customerService).create(customerCreateRequest);
    }


    @Test
    void postCustomer_returns400_whenNameNull() throws Exception {
        // Arrange
        customer.setName(null);
        CustomerCreateRequest wrongCreateRequest = TestDataFactory.newCustomerCreateRequest(customer);

        // Act
        mockMvc.perform(post(ApiPaths.CUSTOMERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(wrongCreateRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(customerService);
    }

    @Test
    void postCustomer_returns409_whenEmailDuplicated() throws Exception {
        // Arrange
        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).create(customerCreateRequest);

        // Act
        mockMvc.perform(post(ApiPaths.CUSTOMERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(customerCreateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(customerService).create(customerCreateRequest);
    }

    @Test
    void getCustomerById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerService.getById(customerId)).willReturn(customerResponse);

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS + "/" + customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(customerResponse.id().toString()))
                .andExpect(jsonPath("$.name").value(customerResponse.name()))
                .andExpect(jsonPath("$.email").value(customerResponse.email()));

        // Assert
        verify(customerService).getById(customerId);
    }

    @Test
    void getCustomerById_returns404_whenMissing() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        given(customerService.getById(customerId)).willThrow(NotFoundException.class);

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS + "/" + customerId))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).getById(customerId);
    }

    @Test
    void listCustomers_returns200_andArray_whenExists() throws Exception {
        // Arrange
        Customer savedCustomer2 = TestDataFactory.newSavedCustomer(TestDataFactory.newCustomer());
        CustomerResponse response2 = TestDataFactory.newCustomerResponse(savedCustomer2);
        List<CustomerResponse> responseList = List.of(customerResponse, response2);

        given(customerService.list()).willReturn(responseList);

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2))
                .andExpect(jsonPath("$[0].id").value(customerResponse.id().toString()))
                .andExpect(jsonPath("$[0].email").value(customerResponse.email()))
                .andExpect(jsonPath("$[1].id").value(response2.id().toString()))
                .andExpect(jsonPath("$[1].email").value(response2.email()));

        // Assert
        verify(customerService).list();
    }

    @Test
    void listCustomers_returns200_andEmptyArray_whenNotExists() throws Exception {
        // Arrange
        given(customerService.list()).willReturn(Collections.emptyList());

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(0));

        // Assert
        verify(customerService).list();
    }

    @Test
    void putCustomer_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerUpdateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Assert
        verify(customerService).update(customerId, customerUpdateRequest);
    }

    @Test
    void putCustomer_returns400_whenNameNull() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        customer.setName(null);
        CustomerUpdateRequest wrongUpdateRequest = TestDataFactory.newCustomerUpdateRequest(customer);

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongUpdateRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(customerService);
    }

    @Test
    void putCustomer_returns404_whenMissing() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).update(customerId, customerUpdateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerUpdateRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).update(customerId, customerUpdateRequest);
    }

    @Test
    void putCustomer_returns409_whenEmailDuplicated() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).update(customerId, customerUpdateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerUpdateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(customerService).update(customerId, customerUpdateRequest);
    }

    @Test
    void patchCustomer_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerPatchRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(customerService).patch(customerId, customerPatchRequest);
    }

    @Test
    void patchCustomer_returns400_whenEmailInvalid() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        CustomerPatchRequest wrongPatchRequest = new CustomerPatchRequest(null, "invalidEmail");

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(wrongPatchRequest)))
                .andExpect(status().isBadRequest());

        // Assert
        verifyNoInteractions(customerService);
    }

    @Test
    void patchCustomer_returns404_whenMissing() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).patch(customerId, customerPatchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerPatchRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).patch(customerId, customerPatchRequest);
    }

    @Test
    void patchCustomer_returns409_whenEmailDuplicated() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).patch(customerId, customerPatchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(customerPatchRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(customerService).patch(customerId, customerPatchRequest);
    }

    @Test
    void deleteCustomer_returns204_whenExists() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();

        // Act
        mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(customerService).delete(customerId);
    }

    @Test
    void deleteCustomer_returns404_whenMissing() throws Exception {
        // Arrange
        UUID customerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).delete(customerId);

        // Act
        mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/" + customerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).delete(customerId);
    }
}
