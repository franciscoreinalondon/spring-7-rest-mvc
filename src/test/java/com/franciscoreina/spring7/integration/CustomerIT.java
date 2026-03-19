package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.dto.request.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.dto.request.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.testdata.IntegrationTestDataFactory;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
public class CustomerIT extends AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    IntegrationTestDataFactory dataFactory;

    @BeforeEach
    void setUp() {
        super.setUp();
        customerRepository.deleteAll();
    }

    // ---------------
    //      CREATE
    // ---------------

    @Test
    void create_whenValidData_returnsCreated() {
        // Arrange
        Customer customer = TestDataFactory.newCustomer();
        CustomerCreateRequest request = TestDataFactory.newCustomerCreateRequest(customer);

        // Act
        EntityExchangeResult<Void> result = postRequest(ApiPaths.CUSTOMERS, request)
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Void.class)
                .returnResult();

        // Assert
        String location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(ApiPaths.CUSTOMERS);
    }

    @Test
    void create_whenNameIsNull_returnsBadRequest() {
        // Arrange
        CustomerCreateRequest request = TestDataFactory.newCustomerCreateRequestNullName();

        // Act + Assert
        postRequest(ApiPaths.CUSTOMERS, request)
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(400);
                });
    }

    @Test
    void create_whenEmailDuplicated_returnsConflict() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        Customer duplicateEmail = TestDataFactory.newCustomer(customer.getEmail());
        CustomerCreateRequest request = TestDataFactory.newCustomerCreateRequest(duplicateEmail);

        // Act + Assert
        postRequest(ApiPaths.CUSTOMERS, request)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(409);
                });
    }

    // ---------------
    //      READ
    // ---------------

    @Test
    void getById_whenIdExists_returnsCustomer() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();

        // Act + Assert
        getRequest(ApiPaths.CUSTOMERS + "/" + customer.getId())
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo(customer.getId());
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
    void list_whenCustomersExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoCustomers();

        // Act + Assert
        getRequest(ApiPaths.CUSTOMERS)
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .value(customerResponseList -> {
                    assertThat(customerResponseList).isNotNull();
                    assertThat(customerResponseList).hasSize(2);
                    assertThat(customerResponseList).allSatisfy(customerResponse -> {
                        assertThat(customerResponse.id()).isNotNull();
                        assertThat(customerResponse.name()).isNotBlank();
                        assertThat(customerResponse.email()).isNotNull();
                    });
                });
    }

    @Test
    void list_whenCustomersNotExists_returnEmptyList() {
        // Act + Assert
        getRequest(ApiPaths.CUSTOMERS)
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .value(customerResponseList -> {
                    assertThat(customerResponseList).isEmpty();
                });
    }

    // ---------------
    //      UPDATE
    // ---------------

    @Test
    void update_whenValidCustomer_returnsNoContentAndUpdatesCustomer() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        customer.setName("Updated Name");
        CustomerUpdateRequest update = TestDataFactory.newCustomerUpdateRequest(customer);

        // Act
        putRequest(ApiPaths.CUSTOMERS + "/" + customer.getId(), update)
                .expectStatus().isNoContent();

        // Assert
        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
    }

    @Test
    void update_whenIdNotExists_returnsNotFound() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        customer.setName("Updated Name");
        CustomerUpdateRequest update = TestDataFactory.newCustomerUpdateRequest(customer);

        // Act + Assert
        putRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID(), update)
                .expectStatus().isNotFound()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message()).contains("Customer not found");
                });
    }

    @Test
    void update_whenNameIsNull_returnsBadRequest() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        customer.setName(null);
        CustomerUpdateRequest update = TestDataFactory.newCustomerUpdateRequest(customer);

        // Act + Assert
        putRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID(), update)
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(400);
                });
    }

    @Test
    void update_whenEmailDuplicated_returnsConflict() {
        // Arrange
        dataFactory.persistTwoCustomers();
        List<Customer> customerList = dataFactory.findTwoCustomers();
        String existingEmail = customerList.getLast().getEmail();

        Customer customer = customerList.getFirst();
        customer.setEmail(existingEmail);
        CustomerUpdateRequest update = TestDataFactory.newCustomerUpdateRequest(customer);

        // Act + Assert
        putRequest(ApiPaths.CUSTOMERS + "/" + customer.getId(), update)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(409);
                });
    }

    @Test
    void patch_whenValidCustomer_returnsNoContentAndUpdatesCustomer() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        CustomerPatchRequest patch = TestDataFactory.newCustomerPatchRequestWithName();

        // Act
        patchRequest(ApiPaths.CUSTOMERS + "/" + customer.getId(), patch)
                .expectStatus().isNoContent();

        // Assert
        Customer updatedCustomer = customerRepository.findById(customer.getId()).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Patch name");
    }

    @Test
    void patch_whenInvalidEmail_returnsBadRequest() {
        // Arrange
        Customer customer = dataFactory.persistCustomer();
        CustomerPatchRequest patch = TestDataFactory.newCustomerPatchRequestInvalidEmail();

        // Act + Assert
        patchRequest(ApiPaths.CUSTOMERS + "/" + customer.getId(), patch)
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(400);
                });
    }

    // ---------------
    //      DELETE
    // ---------------

    @Test
    void delete_whenIdExists_returnsNoContent() {
        // Arrange
        Customer savedCustomer = dataFactory.persistCustomer();

        // Act
        deleteRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId())
                .expectStatus().isNoContent();

        // Assert
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

    // ---------------
    //      AUTH
    // ---------------

    @Test
    void requestWithAuth_returns200() {
        // Act + Assert
        getRequest(ApiPaths.CUSTOMERS)
                .expectStatus().isOk();
    }

    @Test
    void requestWithoutAuth_returns401() {
        // Arrange
        WebTestClient clientWithoutAuth = webTestClient
                .mutate()
                .defaultHeaders(headers -> headers.remove("Authorization"))
                .build();

        // Act + Assert
        clientWithoutAuth.get()
                .uri(ApiPaths.CUSTOMERS)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}