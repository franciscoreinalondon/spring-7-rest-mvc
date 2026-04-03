package com.franciscoreina.spring7.controllers;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.services.CustomerService;
import org.instancio.Instancio;
import org.instancio.Model;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.instancio.Select.field;
import static org.mockito.BDDMockito.given;
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
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private CustomerService customerService;

    private static final UUID CUSTOMER_ID = UUID.randomUUID();

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
        void create_shouldReturnCreated_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var response = createResponse(request.name(), request.email());

            given(customerService.create(request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string(HttpHeaders.LOCATION, ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID))
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.email").value(response.email()));

            verify(customerService).create(request);
        }

        @Test
        void create_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new CustomerRequest("", "invalid-email");

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }

        @Test
        void create_shouldReturnBadRequest_whenFieldsAreNull() throws Exception {
            // Arrange
            var request = new CustomerRequest(null, null);

            // Act + Assert
            mockMvc.perform(post(ApiPaths.CUSTOMERS)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }
    }

    @Nested
    class GetByIdTests {

        @Test
        void getById_shouldReturnOk_whenCustomerExists() throws Exception {
            // Arrange
            var response = createResponse("John Doe", "john@test.com");

            given(customerService.getById(CUSTOMER_ID)).willReturn(response);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.email").value(response.email()));

            verify(customerService).getById(CUSTOMER_ID);
        }
    }

    @Nested
    class SearchTests {

        @Test
        void search_shouldReturnPage_whenNoFiltersAreProvided() throws Exception {
            // Arrange
            var response = createResponse("John Doe", "john@test.com");
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(customerService.search(null, null, PageRequest.of(0, 20)))
                    .willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS)
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                    .andExpect(jsonPath("$.content[0].name").value(response.name()))
                    .andExpect(jsonPath("$.content[0].email").value(response.email()));

            verify(customerService).search(null, null, PageRequest.of(0, 20));
        }

        @Test
        void search_shouldReturnPage_whenFiltersAreProvided() throws Exception {
            // Arrange
            var response = createResponse("John Doe", "john@test.com");
            var page = new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1);

            given(customerService.search("John", "john@test.com", PageRequest.of(0, 20)))
                    .willReturn(page);

            // Act + Assert
            mockMvc.perform(get(ApiPaths.CUSTOMERS)
                            .param("name", "John")
                            .param("email", "john@test.com")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(response.id().toString()))
                    .andExpect(jsonPath("$.content[0].name").value(response.name()))
                    .andExpect(jsonPath("$.content[0].email").value(response.email()));

            verify(customerService).search("John", "john@test.com", PageRequest.of(0, 20));
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_shouldReturnOk_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(REQUEST_MODEL);
            var response = createResponse(request.name(), request.email());

            given(customerService.update(CUSTOMER_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.email").value(response.email()));

            verify(customerService).update(CUSTOMER_ID, request);
        }

        @Test
        void update_shouldReturnBadRequest_whenRequestIsInvalid() throws Exception {
            // Arrange
            var request = new CustomerRequest("", "invalid-email");

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }

        @Test
        void update_shouldReturnBadRequest_whenFieldsAreNull() throws Exception {
            // Arrange
            var request = new CustomerRequest(null, null);

            // Act + Assert
            mockMvc.perform(put(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_shouldReturnOk_whenPatchIsEmpty() throws Exception {
            // Arrange
            var request = new CustomerPatchRequest(null, null);
            var response = createResponse("John Doe", "john@test.com");

            given(customerService.patch(CUSTOMER_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()));

            verify(customerService).patch(CUSTOMER_ID, request);
        }

        @Test
        void patch_shouldReturnOk_whenRequestIsValid() throws Exception {
            // Arrange
            var request = Instancio.create(PATCH_MODEL);
            var response = createResponse(request.name(), request.email());

            given(customerService.patch(CUSTOMER_ID, request)).willReturn(response);

            // Act + Assert
            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(response.id().toString()))
                    .andExpect(jsonPath("$.name").value(response.name()))
                    .andExpect(jsonPath("$.email").value(response.email()));

            verify(customerService).patch(CUSTOMER_ID, request);
        }

        @Test
        void patch_shouldReturnBadRequest_whenEmailFormatIsInvalid() throws Exception {
            var request = new CustomerPatchRequest(null, "invalid-email");

            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }

        @Test
        void patch_shouldReturnBadRequest_whenFieldsAreBlank() throws Exception {
            var request = new CustomerPatchRequest("", "");

            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }

        @Test
        void patch_shouldReturnBadRequest_whenFieldsAreOnlySpaces() throws Exception {
            var request = new CustomerPatchRequest(" ", " ");

            mockMvc.perform(patch(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").exists())
                    .andExpect(jsonPath("$.errors.email").exists());

            verifyNoInteractions(customerService);
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void delete_shouldReturnNoContent_whenCustomerExists() throws Exception {
            // Act + Assert
            mockMvc.perform(delete(ApiPaths.CUSTOMERS + "/" + CUSTOMER_ID))
                    .andExpect(status().isNoContent());

            verify(customerService).delete(CUSTOMER_ID);
        }
    }

    private CustomerResponse createResponse(String name, String email) {
        return new CustomerResponse(
                CUSTOMER_ID,
                Instant.parse("2026-04-03T10:00:00Z"),
                Instant.parse("2026-04-03T10:00:00Z"),
                name,
                email
        );
    }
}
