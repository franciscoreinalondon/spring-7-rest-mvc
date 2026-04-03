package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.CategoryRepository;
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
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
public class MilkIT extends AbstractJwtMockIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    MilkRepository milkRepository;

    @Autowired
    IntegrationTestDataFactory dataFactory;

    Category savedCategory;

    @BeforeEach
    void setUp() {
        super.setUp();
        milkRepository.deleteAll();
        savedCategory = categoryRepository.saveAndFlush(Category.createCategory(UUID.randomUUID().toString()));
    }

    // ---------------
    //      CREATE
    // ---------------

    @Test
    void create_whenValidData_returnsCreated() {
        // Arrange
        var newMilk = TestDataFactory.getNewMilk(savedCategory);
        var createRequest = TestDataFactory.getMilkCreateRequest(newMilk);

        // Act
        var result = postRequest(ApiPaths.MILKS, createRequest)
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Void.class)
                .returnResult();

        // Assert
        var location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(ApiPaths.MILKS);
    }

    @Test
    void create_whenNameIsNull_returnsBadRequest() {
        // Arrange
        var createRequest = TestDataFactory.getMilkCreateRequestNullName();

        // Act + Assert
        postRequest(ApiPaths.MILKS, createRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.name").exists();
    }

    @Test
    void create_whenUpcDuplicated_returnsConflict() {
        // Arrange
        var savedMilk = dataFactory.persistMilk(savedCategory);
        var milkDuplicateUpc = TestDataFactory.getNewMilk(savedMilk.getUpc(), savedMilk.getCategories().iterator().next());
        var createRequest = TestDataFactory.getMilkCreateRequest(milkDuplicateUpc);

        // Act + Assert
        postRequest(ApiPaths.MILKS, createRequest)
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
        var savedMilk = dataFactory.persistMilk(savedCategory);

        // Act + Assert
        getRequest(ApiPaths.MILKS + "/" + savedMilk.getId())
                .expectStatus().isOk()
                .expectBody(MilkResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo(savedMilk.getId());
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
        dataFactory.persistTwoMilks(savedCategory);

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

//    @Test
//    void listByName_whenMilksExists_returnsDataList() {
//        // Arrange
//        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types
//
//        var savedCategory = categoryRepository.save(TestDataFactory.getNewCategory());
//        var newMilk3 = TestDataFactory.getNewMilk(savedCategory);
//        newMilk3.setName("Natural A2");
//        newMilk3.setMilkType(MilkType.A2);
//        milkRepository.saveAndFlush(newMilk3);
//
//        // Act + Assert
//        getRequest(ApiPaths.MILKS, Map.of("name", "a2"))
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content").isArray()
//                .jsonPath("$.content.length()").isEqualTo(1)
//                .jsonPath("$.content[0].name")
//                .value(name -> assertThat(name.toString().toLowerCase()).contains(("a2")));
//    }

//    @Test
//    void listByType_whenMilksExists_returnsDataList() {
//        // Arrange
//        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types
//
//        var savedCategory = categoryRepository.save(TestDataFactory.getNewCategory());
//        var newMilk3 = TestDataFactory.getNewMilk(savedCategory);
//        newMilk3.setMilkType(MilkType.A2);
//        milkRepository.saveAndFlush(newMilk3);
//
//        // Act + Assert
//        getRequest(ApiPaths.MILKS, Map.of("milkType", "A2"))
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content").isArray()
//                .jsonPath("$.content.length()").isEqualTo(1)
//                .jsonPath("$.content[0].milkType").isEqualTo(MilkType.A2);
//    }

//    @Test
//    void listByNameAndType_whenMilksExists_returnsDataList() {
//        // Arrange
//        dataFactory.persistTwoMilks(); // SEMI_SKIMMED milk types
//
//        var savedCategory = categoryRepository.save(TestDataFactory.getNewCategory());
//        var newMilk3 = TestDataFactory.getNewMilk(savedCategory);
//        newMilk3.setName("Natural A2");
//        newMilk3.setMilkType(MilkType.A2);
//        milkRepository.saveAndFlush(newMilk3);
//
//        // Act + Assert
//        getRequest(ApiPaths.MILKS, Map.of("name", "natural", "milkType", "A2"))
//                .expectStatus().isOk()
//                .expectBody()
//                .jsonPath("$.content.length()").isEqualTo(1)
//                .jsonPath("$.content[0].name")
//                .value(name -> assertThat(name.toString().toLowerCase()).contains("a2"))
//                .jsonPath("$.content[0].milkType").isEqualTo(MilkType.A2);
//    }

//    @Test
    void listByNameAndTypeUsingPage1_whenMilksExists_returnsDataList() throws FileNotFoundException {
        // Arrange
//        dataFactory.loadMilkCsvDataset(savedCategory);

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

//    @Test
//    void update_whenValidMilk_returnsNoContentAndUpdatesMilk() {
//        // Arrange
//        var savedMilk = dataFactory.persistMilk();
//        savedMilk.setName("Updated Name");
//        var updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
//
//        // Act
//        putRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), updateRequest)
//                .expectStatus().isNoContent();
//
//        // Assert
//        var updatedMilk = milkRepository.findById(savedMilk.getId()).orElseThrow();
//        assertThat(updatedMilk.getName()).isEqualTo("Updated Name");
//    }

//    @Test
//    void update_whenIdNotExists_returnsNotFound() {
//        // Arrange
//        var savedMilk = dataFactory.persistMilk();
//        savedMilk.setName("Updated Name");
//        var updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
//
//        // Act + Assert
//        putRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), updateRequest)
//                .expectStatus().isNotFound()
//                .expectBody(ApiError.class)
//                .value(error -> {
//                    assertThat(error).isNotNull();
//                    assertThat(error.status()).isEqualTo(404);
//                    assertThat(error.message()).contains("Milk not found");
//                });
//    }

//    @Test
//    void update_whenNameIsNull_returnsBadRequest() {
//        // Arrange
//        var savedMilk = dataFactory.persistMilk();
//        savedMilk.setName(null);
//        var updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
//
//        // Act + Assert
//        putRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), updateRequest)
//                .expectStatus().isBadRequest()
//                .expectBody(ApiError.class)
//                .value(error -> {
//                    assertThat(error).isNotNull();
//                    assertThat(error.status()).isEqualTo(400);
//                });
//    }

    //TBF
//    @Test
//    void update_whenUpcDuplicated_returnsConflict() {
//        // Arrange
//        dataFactory.persistTwoMilks();
//
//        var savedMilkList = dataFactory.findTwoMilks();
//        var existingUpc = savedMilkList.getLast().getUpc();
//
//        var savedMilk = savedMilkList.getFirst();
//        savedMilk.setUpc(existingUpc);
//        var updateRequest = TestDataFactory.getMilkUpdateRequest(savedMilk);
//
//        // Act + Assert
//        putRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), updateRequest)
//                .expectStatus().isEqualTo(HttpStatus.CONFLICT)
//                .expectBody(ApiError.class)
//                .value(error -> {
//                    assertThat(error).isNotNull();
//                    assertThat(error.status()).isEqualTo(409);
//                });
//    }

//    @Test
//    void patch_whenValidMilk_returnsNoContentAndUpdatesMilk() {
//        // Arrange
//        var savedMilk = dataFactory.persistMilk();
//        var patchRequest = TestDataFactory.getMilkPatchRequestWithName();
//
//        // Act
//        patchRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), patchRequest)
//                .expectStatus().isNoContent();
//
//        // Assert
//        var updatedMilk = milkRepository.findById(savedMilk.getId()).orElseThrow();
//        assertThat(updatedMilk.getName()).isEqualTo("Patch name");
//    }

    @Test
    void patch_whenInvalidUpc_returnsBadRequest() {
        // Arrange
        var savedMilk = dataFactory.persistMilk(savedCategory);
        var patchRequest = TestDataFactory.getMilkPatchRequestInvalidUpc();

        // Act + Assert
        patchRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), patchRequest)
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errors.upc").exists();
    }

    // ---------------
    //      DELETE
    // ---------------

    @Test
    void delete_whenIdExists_returnsNoContent() {
        // Arrange
        var savedMilk = dataFactory.persistMilk(savedCategory);

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
        var clientWithoutAuth = webTestClient
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
