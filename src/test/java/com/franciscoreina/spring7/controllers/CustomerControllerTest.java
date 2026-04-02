package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.NotFoundException;
import com.franciscoreina.spring7.services.CustomerService;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
        void postCustomer_returns201_whenValidRequest() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var response = Instancio.create(CustomerResponse.class);

            given(customerService.create(request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", ApiPaths.CUSTOMERS + "/" + response.id()))
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        void getCustomerById_returns200_whenExists() throws Exception {
            // Arrange
            var response = Instancio.create(CustomerResponse.class);

            given(customerService.getById(response.id())).willReturn(response);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS + "/{id}", response.id()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()));
        }

        @Test
        void searchCustomers_returns200_withData() throws Exception {
            // Arrange
            var page = new PageImpl<>(Instancio.ofList(CustomerResponse.class).size(2).create());

            given(customerService.search(any(), any(), any())).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.size()").value(2));
        }

        @Test
        void searchCustomers_returns200_whenFilteredByNameAndEmail() throws Exception {
            var page = new PageImpl<>(Instancio.ofList(CustomerResponse.class).size(2).create());

            // Arrange
            given(customerService.search(eq("Customer name"), eq("test@email.com"), any())).willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS).param("name", "Customer name").param("email", "test@email.com"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content.size()").value(2));
        }

        @Test
        void putCustomer_returns200_whenExists() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);
            var response = Instancio.of(CustomerResponse.class).set(field(CustomerResponse::id), id).create();

            given(customerService.update(id, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void patchCustomer_returns200_whenExists() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);
            var response = Instancio.of(CustomerResponse.class).set(field(CustomerResponse::id), id).create();

            given(customerService.patch(id, patch)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()));
        }

        @Test
        void deleteCustomer_returns204() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            // Act + Assert
            mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/{id}", id))
                    .andExpect(status().isNoContent());

            // Assert
            verify(customerService).delete(id);
        }
    }

    // ---------------
    //    NEGATIVE
    // ---------------

    @Nested
    class NegativeTests {

        @Test
        void postCustomer_returns400_whenInvalidData() throws Exception {
            // Arrange
            var invalidRequest = new CustomerRequest(" ", "Invalid email");

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            // Assert
            verifyNoInteractions(customerService);
        }

        @Test
        void postCustomer_returns409_whenDuplicatedEmail() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);

            given(customerService.create(request)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void getCustomer_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            given(customerService.getById(id)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS + "/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        void putCustomer_returns400_whenInvalidData() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = new CustomerRequest(" ", "Invalid email");

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            // Assert
            verifyNoInteractions(customerService);
        }

        @Test
        void putCustomer_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);

            given(customerService.update(id, request)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void putCustomer_returns409_whenDuplicatedEmail() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var request = Instancio.create(REQUEST_MODEL);

            given(customerService.update(id, request)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void patchCustomer_returns400_whenInvalidData() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = new CustomerRequest(null, "Invalid email");

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());

            // Assert
            verifyNoInteractions(customerService);
        }

        @Test
        void patchCustomer_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var patch = Instancio.create(PATCH_MODEL);

            given(customerService.patch(id, patch)).willThrow(new NotFoundException("Not found"));

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(patch)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void patchCustomer_returns409_whenEmailDuplicated() throws Exception {
            // Arrange
            var id = UUID.randomUUID();
            var invalidRequest = Instancio.create(PATCH_MODEL);

            given(customerService.patch(id, invalidRequest)).willThrow(new DataIntegrityViolationException("Conflict"));

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/{id}", id)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isConflict());
        }

        @Test
        void deleteCustomer_returns404_whenNotFound() throws Exception {
            // Arrange
            var id = UUID.randomUUID();

            willThrow(new NotFoundException("Not found")).given(customerService).delete(id);

            // Act + Assert
            mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/{id}", id)
                            .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());

            // Assert
            verify(customerService).delete(id);
        }
    }
}
