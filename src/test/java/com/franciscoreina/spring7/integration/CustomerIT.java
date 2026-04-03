package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.testdata.IntegrationTestDataFactory;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
class CustomerIT extends AbstractJwtMockIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private IntegrationTestDataFactory dataFactory;

    @BeforeEach
    void setUp() {
        super.setUp();
        customerRepository.deleteAll();
    }

    @Nested
    class CreateTests {

        @Test
        void create_whenValidData_returnsCreatedAndPersistsCustomer() {
            // Arrange
            var newCustomer = TestDataFactory.getNewCustomer();
            var createRequest = TestDataFactory.getCustomerCreateRequest(newCustomer);

            // Act + Assert
            var response = postRequest(ApiPaths.CUSTOMERS, createRequest)
                    .expectStatus().isCreated()
                    .expectHeader().exists(HttpHeaders.LOCATION)
                    .expectBody(CustomerResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();
            assertThat(response.name()).isEqualTo(createRequest.name());
            assertThat(response.email()).isEqualTo(createRequest.email());
            assertThat(customerRepository.existsByEmailIgnoreCase(createRequest.email())).isTrue();
        }

        @Test
        void create_whenNameIsNull_returnsBadRequest() {
            // Arrange
            var createRequest = TestDataFactory.getCustomerCreateRequestNullName();

            // Act + Assert
            postRequest(ApiPaths.CUSTOMERS, createRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.name").exists();
        }

        @Test
        void create_whenEmailDuplicated_returnsConflict() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var duplicatedCustomer = TestDataFactory.getNewCustomer(savedCustomer.getEmail());
            var createRequest = TestDataFactory.getCustomerCreateRequest(duplicatedCustomer);

            // Act + Assert
            postRequest(ApiPaths.CUSTOMERS, createRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Customer email already exists");

                    });
        }
    }

    @Nested
    class ReadTests {

        @Test
        void getById_whenIdExists_returnsCustomer() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();

            // Act + Assert
            getRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId())
                    .expectStatus().isOk()
                    .expectBody(CustomerResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedCustomer.getId());
                        assertThat(response.name()).isEqualTo(savedCustomer.getName());
                        assertThat(response.email()).isEqualTo(savedCustomer.getEmail());
                    });
        }

        @Test
        void getById_whenIdNotExists_returnsNotFound() {
            // Act + Assert
            getRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID())
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Customer not found");
                    });
        }

        @Test
        void search_whenCustomersExist_returnsCustomerPage() {
            // Arrange
            dataFactory.persistTwoCustomers();

            // Act + Assert
            getRequest(ApiPaths.CUSTOMERS)
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content").isArray()
                    .jsonPath("$.content.length()").isEqualTo(2)
                    .jsonPath("$.content[*].id").isNotEmpty()
                    .jsonPath("$.content[*].name").isNotEmpty()
                    .jsonPath("$.content[*].email").isNotEmpty();
        }

        @Test
        void search_whenCustomersDoNotExist_returnsEmptyPage() {
            // Act + Assert
            getRequest(ApiPaths.CUSTOMERS)
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content").isArray()
                    .jsonPath("$.content.length()").isEqualTo(0);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_whenValidCustomer_returnsOkAndUpdatesCustomer() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            savedCustomer.renameTo("Updated Name");
            var updateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);

            // Act + Assert
            putRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), updateRequest)
                    .expectStatus().isOk()
                    .expectBody(CustomerResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedCustomer.getId());
                        assertThat(response.name()).isEqualTo("Updated Name");
                    });

            var updatedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
            assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
        }

        @Test
        void update_whenInvalidData_returnsBadRequest() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var invalidRequest = new CustomerRequest("", "invalid-email");

            // Act + Assert
            putRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), invalidRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.name").exists()
                    .jsonPath("$.errors.email").exists();
        }

        @Test
        void update_whenIdNotExists_returnsNotFound() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var updateRequest = new CustomerRequest("Updated Name", savedCustomer.getEmail());

            // Act + Assert
            putRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID(), updateRequest)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Customer not found");
                    });
        }

        @Test
        void update_whenEmailDuplicated_returnsConflict() {
            // Arrange
            dataFactory.persistTwoCustomers();
            var customers = dataFactory.findTwoCustomers();

            var existingEmail = customers.getLast().getEmail();
            var customerToUpdate = customers.getFirst();
            customerToUpdate.changeEmailTo(existingEmail);

            var updateRequest = TestDataFactory.getCustomerUpdateRequest(customerToUpdate);

            // Act + Assert
            putRequest(ApiPaths.CUSTOMERS + "/" + customerToUpdate.getId(), updateRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Customer email already exists");
                    });
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_whenValidCustomer_returnsOkAndUpdatesCustomer() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var patchRequest = TestDataFactory.getCustomerPatchRequestWithName();

            // Act + Assert
            patchRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), patchRequest)
                    .expectStatus().isOk()
                    .expectBody(CustomerResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedCustomer.getId());
                        assertThat(response.name()).isEqualTo("Patch name");
                    });

            var updatedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
            assertThat(updatedCustomer.getName()).isEqualTo("Patch name");
        }

        @Test
        void patch_whenBlankName_returnsBadRequest() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var patchRequest = new CustomerPatchRequest(" ", null);

            // Act + Assert
            patchRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), patchRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.name").exists();
        }

        @Test
        void patch_whenInvalidEmail_returnsBadRequest() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var patchRequest = TestDataFactory.getCustomerPatchRequestInvalidEmail();

            // Act + Assert
            patchRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), patchRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.email").exists();
        }

        @Test
        void patch_whenIdNotExists_returnsNotFound() {
            // Arrange
            var patchRequest = TestDataFactory.getCustomerPatchRequestWithName();

            // Act + Assert
            patchRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID(), patchRequest)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Customer not found");
                    });
        }

        @Test
        void patch_whenEmailDuplicated_returnsConflict() {
            // Arrange
            dataFactory.persistTwoCustomers();
            var customers = dataFactory.findTwoCustomers();

            var existingEmail = customers.getLast().getEmail();
            var customerToPatch = customers.getFirst();

            var patchRequest = new CustomerPatchRequest(null, existingEmail);

            // Act + Assert
            patchRequest(ApiPaths.CUSTOMERS + "/" + customerToPatch.getId(), patchRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Customer email already exists");
                    });
        }
    }

    @Nested
    class DeleteTests {

        @Test
        void delete_whenIdExists_returnsNoContent() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();

            // Act + Assert
            deleteRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId())
                    .expectStatus().isNoContent();

            assertThat(customerRepository.existsById(savedCustomer.getId())).isFalse();
        }

        @Test
        void delete_whenIdNotExists_returnsNotFound() {
            // Act + Assert
            deleteRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID())
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Customer not found");
                    });
        }

    }

    @Nested
    class AuthTests {

        @Test
        void requestWithAuth_returnsOk() {
            // Act + Assert
            getRequest(ApiPaths.CUSTOMERS)
                    .expectStatus().isOk();
        }

        @Test
        void requestWithoutAuth_returnsUnauthorized() {
            // Arrange
            var clientWithoutAuth = webTestClient.mutate()
                    .defaultHeaders(headers -> headers.remove(HttpHeaders.AUTHORIZATION))
                    .build();

            // Act + Assert
            clientWithoutAuth.get()
                    .uri(ApiPaths.CUSTOMERS)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
}