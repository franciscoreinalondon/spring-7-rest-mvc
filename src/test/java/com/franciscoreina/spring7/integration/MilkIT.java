package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.domain.MilkType;
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
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;
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
        super.setUp();
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
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[*].id").isNotEmpty()
                .jsonPath("$.content[*].name").isNotEmpty()
                .jsonPath("$.content[*].milkType").isNotEmpty()
                .jsonPath("$.content[*].price").isNotEmpty()
                .jsonPath("$.content[*].stock").isNotEmpty();
    }

    @Test
    void listByName_whenMilksExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types

        Milk milk3 = TestDataFactory.newMilk();
        milk3.setName("Natural A2");
        milk3.setMilkType(MilkType.A2);
        milkRepository.saveAndFlush(milk3);

        // Act + Assert
        getRequest(ApiPaths.MILKS, Map.of("name", "a2"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].name")
                .value(name -> assertThat(name.toString().toLowerCase()).contains(("a2")));
    }

    @Test
    void listByType_whenMilksExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types

        Milk third = TestDataFactory.newMilk();
        third.setMilkType(MilkType.A2);
        milkRepository.saveAndFlush(third);

        // Act + Assert
        getRequest(ApiPaths.MILKS, Map.of("milkType", "A2"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].milkType").isEqualTo(MilkType.A2);
    }

    @Test
    void listByNameAndType_whenMilksExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types

        Milk milk3 = TestDataFactory.newMilk();
        milk3.setName("Natural A2");
        milk3.setMilkType(MilkType.A2);
        milkRepository.saveAndFlush(milk3);

        // Act + Assert
        getRequest(ApiPaths.MILKS, Map.of("name", "natural", "milkType", "A2"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(1)
                .jsonPath("$.content[0].name")
                .value(name -> assertThat(name.toString().toLowerCase()).contains("a2"))
                .jsonPath("$.content[0].milkType").isEqualTo(MilkType.A2);
    }

    @Test
    void listByNameAndTypeUsingPage1_whenMilksExists_returnsDataList() throws FileNotFoundException {
        // Arrange
        dataFactory.loadMilkCsvDataset();

        // Act + Assert
        getRequest(ApiPaths.MILKS, Map.of("name", "skimmed", "milkType", "SKIMMED",
                "page", "1", "size", "50"))
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(12);
    }

    @Test
    void list_whenMilksNotExists_returnEmptyList() {
        // Act + Assert
        getRequest(ApiPaths.MILKS)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content.length()").isEqualTo(0);
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

    // ---------------
    //      AUTH
    // ---------------

    @Test
    void requestWithAuth_returns200() {
        // Act + Assert
        getRequest(ApiPaths.MILKS)
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
                .uri(ApiPaths.MILKS)
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
