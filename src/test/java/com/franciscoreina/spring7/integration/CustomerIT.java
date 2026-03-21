package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.dto.request.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
        var newCustomer = TestDataFactory.getNewCustomer();
        var createRequest = TestDataFactory.getCustomerCreateRequest(newCustomer);

        // Act
        var result = postRequest(ApiPaths.CUSTOMERS, createRequest)
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Void.class)
                .returnResult();

        // Assert
        var location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(ApiPaths.CUSTOMERS);
    }

    @Test
    void create_whenNameIsNull_returnsBadRequest() {
        // Arrange
        var createRequest = TestDataFactory.getCustomerCreateRequestNullName();

        // Act + Assert
        postRequest(ApiPaths.CUSTOMERS, createRequest)
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
        var savedCustomer = dataFactory.persistCustomer();
        var customerDuplicateEmail = TestDataFactory.getNewCustomer(savedCustomer.getEmail());
        var createRequest = TestDataFactory.getCustomerCreateRequest(customerDuplicateEmail);

        // Act + Assert
        postRequest(ApiPaths.CUSTOMERS, createRequest)
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
        var savedCustomer = dataFactory.persistCustomer();

        // Act + Assert
        getRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId())
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo(savedCustomer.getId());
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
        var savedCustomer = dataFactory.persistCustomer();
        savedCustomer.renameTo("Updated Name");
        var updateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);

        // Act
        putRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), updateRequest)
                .expectStatus().isNoContent();

        // Assert
        var updatedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Updated Name");
    }

    @Test
    void update_whenIdNotExists_returnsNotFound() {
        // Arrange
        var savedCustomer = dataFactory.persistCustomer();
        var updateRequest = new CustomerUpdateRequest("Updated Name", savedCustomer.getEmail());

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

//    @Test
//    void update_whenNameIsNull_returnsBadRequest() {
//        // Arrange
//        var savedCustomer = dataFactory.persistCustomer();
//        savedCustomer.updateName(null);
//        var updateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);
//
//        // Act + Assert
//        putRequest(ApiPaths.CUSTOMERS + "/" + UUID.randomUUID(), updateRequest)
//                .expectStatus().isBadRequest()
//                .expectBody(ApiError.class)
//                .value(error -> {
//                    assertThat(error).isNotNull();
//                    assertThat(error.status()).isEqualTo(400);
//                });
//    }

    @Test
    void update_whenEmailDuplicated_returnsConflict() {
        // Arrange
        dataFactory.persistTwoCustomers();
        var savedCustomerList = dataFactory.findTwoCustomers();
        var existingEmail = savedCustomerList.getLast().getEmail();

        var savedCustomer = savedCustomerList.getFirst();
        savedCustomer.changeEmailTo(existingEmail);
        var updateRequest = TestDataFactory.getCustomerUpdateRequest(savedCustomer);

        // Act + Assert
        putRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), updateRequest)
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
        var savedCustomer = dataFactory.persistCustomer();
        var patchRequest = TestDataFactory.getCustomerPatchRequestWithName();

        // Act
        patchRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), patchRequest)
                .expectStatus().isNoContent();

        // Assert
        var updatedCustomer = customerRepository.findById(savedCustomer.getId()).orElseThrow();
        assertThat(updatedCustomer.getName()).isEqualTo("Patch name");
    }

    @Test
    void patch_whenInvalidEmail_returnsBadRequest() {
        // Arrange
        var savedCustomer = dataFactory.persistCustomer();
        var patchRequest = TestDataFactory.getCustomerPatchRequestInvalidEmail();

        // Act + Assert
        patchRequest(ApiPaths.CUSTOMERS + "/" + savedCustomer.getId(), patchRequest)
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
        var savedCustomer = dataFactory.persistCustomer();

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
        var clientWithoutAuth = webTestClient
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