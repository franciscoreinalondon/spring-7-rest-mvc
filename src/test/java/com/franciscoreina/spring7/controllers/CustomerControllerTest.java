package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
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
    void postCustomer_returns201_andLocationHeader_whenRequestValid() throws Exception {
        // Arrange
        given(customerService.create(createRequest)).willReturn(response);

        // Act
        mockMvc.perform(post(ApiPaths.CUSTOMERS)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", ApiPaths.CUSTOMERS + "/" + response.id()));

        // Assert
        verify(customerService).create(createRequest);
    }


//    @Test
//    void postCustomer_returns400_whenNameNull() throws Exception {
//        // Arrange
//        newCustomer.updateName(null);
//        var wrongCreateRequest = TestDataFactory.getCustomerCreateRequest(newCustomer);
//
//        // Act
//        mockMvc.perform(post(ApiPaths.CUSTOMERS)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsBytes(wrongCreateRequest)))
//                .andExpect(status().isBadRequest());
//
//        // Assert
//        verifyNoInteractions(customerService);
//    }

//    @Test
//    void postCustomer_returns409_whenEmailDuplicated() throws Exception {
//        // Arrange
//        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).create(createRequest);
//
//        // Act
//        mockMvc.perform(post(ApiPaths.CUSTOMERS)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsBytes(createRequest)))
//                .andExpect(status().isConflict());
//
//        // Assert
//        verify(customerService).create(createRequest);
//    }

    @Test
    void getCustomerById_returns200_andBody_whenExists() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerService.getById(savedCustomerId)).willReturn(response);

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS + "/" + savedCustomerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(response.id().toString()))
                .andExpect(jsonPath("$.name").value(response.name()))
                .andExpect(jsonPath("$.email").value(response.email()));

        // Assert
        verify(customerService).getById(savedCustomerId);
    }

    @Test
    void getCustomerById_returns404_whenMissing() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        given(customerService.getById(savedCustomerId)).willThrow(NotFoundException.class);

        // Act
        mockMvc.perform(get(ApiPaths.CUSTOMERS + "/" + savedCustomerId))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).getById(savedCustomerId);
    }

//    @Test
//    void listCustomers_returns200_andArray_whenExists() throws Exception {
//        // Arrange
//        var savedCustomer2 = TestDataFactory.getSavedCustomer(TestDataFactory.getNewCustomer());
//        var response2 = TestDataFactory.getCustomerResponse(savedCustomer2);
//        var responseList = List.of(response, response2);
//
//        given(customerService.list(, , )).willReturn(responseList);
//
//        // Act
//        mockMvc.perform(get(ApiPaths.CUSTOMERS))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.size()").value(2))
//                .andExpect(jsonPath("$[0].id").value(response.id().toString()))
//                .andExpect(jsonPath("$[0].email").value(response.email()))
//                .andExpect(jsonPath("$[1].id").value(response2.id().toString()))
//                .andExpect(jsonPath("$[1].email").value(response2.email()));
//
//        // Assert
//        verify(customerService).list(, , );
//    }

//    @Test
//    void listCustomers_returns200_andEmptyArray_whenNotExists() throws Exception {
//        // Arrange
//        given(customerService.list(, , )).willReturn(Collections.emptyList());
//
//        // Act
//        mockMvc.perform(get(ApiPaths.CUSTOMERS))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.size()").value(0));
//
//        // Assert
//        verify(customerService).list(, , );
//    }

    @Test
    void putCustomer_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        // Assert
        verify(customerService).update(savedCustomerId, updateRequest);
    }

//    @Test
//    void putCustomer_returns400_whenNameNull() throws Exception {
//        // Arrange
//        var savedCustomerId = savedCustomer.getId();
//        savedCustomer.updateName(null);
//        var wrongUpdateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);
//
//        // Act
//        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
//                        .accept(MediaType.APPLICATION_JSON)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(wrongUpdateRequest)))
//                .andExpect(status().isBadRequest());
//
//        // Assert
//        verifyNoInteractions(customerService);
//    }

    @Test
    void putCustomer_returns404_whenMissing() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).update(savedCustomerId, updateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).update(savedCustomerId, updateRequest);
    }

    @Test
    void putCustomer_returns409_whenEmailDuplicated() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).update(savedCustomerId, updateRequest);

        // Act
        mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(customerService).update(savedCustomerId, updateRequest);
    }

    @Test
    void patchCustomer_returns204_whenRequestValid_andExists() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(customerService).patch(savedCustomerId, patchRequest);
    }

    @Test
    void patchCustomer_returns400_whenEmailInvalid() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        var wrongPatchRequest = TestDataFactory.getCustomerPatchRequestInvalidEmail();

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
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
        var savedCustomerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).patch(savedCustomerId, patchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).patch(savedCustomerId, patchRequest);
    }

    @Test
    void patchCustomer_returns409_whenEmailDuplicated() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        willThrow(new DataIntegrityViolationException("Email Duplicated")).given(customerService).patch(savedCustomerId, patchRequest);

        // Act
        mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(patchRequest)))
                .andExpect(status().isConflict());

        // Assert
        verify(customerService).patch(savedCustomerId, patchRequest);
    }

    @Test
    void deleteCustomer_returns204_whenExists() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();

        // Act
        mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));


        // Assert
        verify(customerService).delete(savedCustomerId);
    }

    @Test
    void deleteCustomer_returns404_whenMissing() throws Exception {
        // Arrange
        var savedCustomerId = savedCustomer.getId();
        willThrow(NotFoundException.class).given(customerService).delete(savedCustomerId);

        // Act
        mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/" + savedCustomerId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Assert
        verify(customerService).delete(savedCustomerId);
    }
}
