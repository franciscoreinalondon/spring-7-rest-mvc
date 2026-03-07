package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.dtos.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dtos.milk.MilkResponse;
import com.franciscoreina.spring7.dtos.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.MilkRepository;
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
public class MilkIT extends AbstractIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    MilkRepository milkRepository;

    @Autowired
    IntegrationTestDataFactory dataFactory;

    @BeforeEach
    void setUp() {
        milkRepository.deleteAll();
    }

    // ---------------
    //      CREATE
    // ---------------

    @Test
    void create_whenValidData_returnsCreated() {
        // Arrange
        Milk milk = TestDataFactory.newMilk();
        MilkCreateRequest request = TestDataFactory.newMilkCreateRequest(milk);

        // Act
        EntityExchangeResult<Void> result = postRequest(ApiPaths.MILKS, request)
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Void.class)
                .returnResult();

        // Assert
        String location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(ApiPaths.MILKS);
    }

    @Test
    void create_whenNameIsNull_returnsBadRequest() {
        // Arrange
        MilkCreateRequest request = TestDataFactory.newMilkCreateRequestNullName();

        // Act + Assert
        postRequest(ApiPaths.MILKS, request)
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(400);
                });
    }

    @Test
    void create_whenUpcDuplicated_returnsConflict() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        Milk duplicateUpc = TestDataFactory.newMilk(milk.getUpc());
        MilkCreateRequest request = TestDataFactory.newMilkCreateRequest(duplicateUpc);

        // Act + Assert
        postRequest(ApiPaths.MILKS, request)
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
    void getById_whenIdExists_returnsMilk() {
        // Arrange
        Milk milk = dataFactory.persistMilk();

        // Act + Assert
        getRequest(ApiPaths.MILKS + "/" + milk.getId())
                .expectStatus().isOk()
                .expectBody(MilkResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo(milk.getId());
                });
    }

    @Test
    void getById_whenIdNotExists_returnsNotFound() {
        // Act + Assert
        getRequest(ApiPaths.MILKS + "/" + UUID.randomUUID())
                .expectStatus().isNotFound()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message()).contains("Milk not found");
                });
    }

    @Test
    void list_whenMilksExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoMilks();

        // Act + Assert
        getRequest(ApiPaths.MILKS)
                .expectStatus().isOk()
                .expectBodyList(MilkResponse.class)
                .value(milkResponseList -> {
                    assertThat(milkResponseList).isNotNull();
                    assertThat(milkResponseList).hasSize(2);
                    assertThat(milkResponseList).allSatisfy(milk -> {
                        assertThat(milk.id()).isNotNull();
                        assertThat(milk.name()).isNotBlank();
                        assertThat(milk.milkType()).isNotNull();
                        assertThat(milk.price()).isNotNull();
                        assertThat(milk.stock()).isNotNull();
                    });
                });
    }

    @Test
    void list_whenMilksNotExists_returnEmptyList() {
        // Act + Assert
        getRequest(ApiPaths.MILKS)
                .expectStatus().isOk()
                .expectBodyList(MilkResponse.class)
                .value(milkResponseList -> {
                    assertThat(milkResponseList).isEmpty();
                });
    }

    // ---------------
    //      UPDATE
    // ---------------

    @Test
    void update_whenValidMilk_returnsNoContentAndUpdatesMilk() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        milk.setName("Updated Name");
        MilkUpdateRequest update = TestDataFactory.newMilkUpdateRequest(milk);

        // Act
        putRequest(ApiPaths.MILKS + "/" + milk.getId(), update)
                .expectStatus().isNoContent();

        // Assert
        Milk updatedMilk = milkRepository.findById(milk.getId()).orElseThrow();
        assertThat(updatedMilk.getName()).isEqualTo("Updated Name");
    }

    @Test
    void update_whenIdNotExists_returnsNotFound() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        milk.setName("Updated Name");
        MilkUpdateRequest update = TestDataFactory.newMilkUpdateRequest(milk);

        // Act + Assert
        putRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), update)
                .expectStatus().isNotFound()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message()).contains("Milk not found");
                });
    }

    @Test
    void update_whenNameIsNull_returnsBadRequest() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        milk.setName(null);
        MilkUpdateRequest update = TestDataFactory.newMilkUpdateRequest(milk);

        // Act + Assert
        putRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), update)
                .expectStatus().isBadRequest()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(400);
                });
    }

    @Test
    void update_whenUpcDuplicated_returnsConflict() {
        // Arrange
        dataFactory.persistTwoMilks();
        List<Milk> milkList = dataFactory.findTwoMilks();
        String existingUpc = milkList.getLast().getUpc();

        Milk milk = milkList.getFirst();
        milk.setUpc(existingUpc);
        MilkUpdateRequest update = TestDataFactory.newMilkUpdateRequest(milk);

        // Act + Assert
        putRequest(ApiPaths.MILKS + "/" + milk.getId(), update)
                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(409);
                });
    }

    @Test
    void patch_whenValidMilk_returnsNoContentAndUpdatesMilk() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        MilkPatchRequest patch = TestDataFactory.newMilkPatchRequestWithName();

        // Act
        patchRequest(ApiPaths.MILKS + "/" + milk.getId(), patch)
                .expectStatus().isNoContent();

        // Assert
        Milk updatedMilk = milkRepository.findById(milk.getId()).orElseThrow();
        assertThat(updatedMilk.getName()).isEqualTo("Patch name");
    }

    @Test
    void patch_whenInvalidUpc_returnsBadRequest() {
        // Arrange
        Milk milk = dataFactory.persistMilk();
        MilkPatchRequest patch = TestDataFactory.newMilkPatchRequestInvalidUpc();

        // Act + Assert
        patchRequest(ApiPaths.MILKS + "/" + milk.getId(), patch)
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
        Milk savedMilk = dataFactory.persistMilk();

        // Act
        deleteRequest(ApiPaths.MILKS + "/" + savedMilk.getId())
                .expectStatus().isNoContent();

        // Assert
        assertThat(milkRepository.existsById(savedMilk.getId())).isFalse();
    }

    @Test
    void delete_whenIdNotExists_returnsNotFound() {
        // Act + Assert
        deleteRequest(ApiPaths.MILKS + "/" + UUID.randomUUID())
                .expectStatus().isNotFound()
                .expectBody(ApiError.class)
                .value(error -> {
                    assertThat(error).isNotNull();
                    assertThat(error.status()).isEqualTo(404);
                    assertThat(error.message()).contains("Milk not found");
                });
    }
}
