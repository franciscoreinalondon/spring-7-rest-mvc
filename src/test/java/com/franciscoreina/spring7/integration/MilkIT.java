package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
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

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
class MilkIT extends AbstractJwtMockIntegrationTest {

    @Container
    @ServiceConnection
    static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private IntegrationTestDataFactory dataFactory;

    private Category savedCategory;

    @BeforeEach
    void setUp() {
        super.setUp();
        milkRepository.deleteAll();
        categoryRepository.deleteAll();
        savedCategory = categoryRepository.saveAndFlush(Category.createCategory(UUID.randomUUID().toString()));
    }

    @Nested
    class CreateTests {

        @Test
        void create_whenValidData_returnsCreatedAndPersistsMilk() {
            // Arrange
            var newMilk = TestDataFactory.newMilk(savedCategory);
            var createRequest = TestDataFactory.milkRequest(newMilk);

            // Act + Assert
            var response = postRequest(ApiPaths.MILKS, createRequest)
                    .expectStatus().isCreated()
                    .expectHeader().exists(HttpHeaders.LOCATION)
                    .expectBody(MilkResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();
            assertThat(response.name()).isEqualTo(createRequest.name());
            assertThat(response.milkType()).isEqualTo(createRequest.milkType());
            assertThat(response.upc()).isEqualTo(createRequest.upc());
            assertThat(milkRepository.existsByUpcIgnoreCase(createRequest.upc())).isTrue();
        }

        @Test
        void create_whenNameIsNull_returnsBadRequest() {
            // Arrange
            var createRequest = TestDataFactory.milkRequestWithNullName();

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
            var duplicatedMilk = TestDataFactory.newMilk(savedMilk.getUpc(), savedCategory);
            var createRequest = TestDataFactory.milkRequest(duplicatedMilk);

            // Act + Assert
            postRequest(ApiPaths.MILKS, createRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Milk UPC already exists:");
                    });
        }
    }

    @Nested
    class ReadTests {

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
                        assertThat(response.name()).isEqualTo(savedMilk.getName());
                        assertThat(response.milkType()).isEqualTo(savedMilk.getMilkType());
                        assertThat(response.upc()).isEqualTo(savedMilk.getUpc());
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
        void search_whenMilksExists_returnsDataList() {
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

        @Test
        void search_whenMilksDoNotExist_returnsEmptyPage() {
            // Act + Assert
            getRequest(ApiPaths.MILKS)
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content").isArray()
                    .jsonPath("$.content.length()").isEqualTo(0);
        }
    }

    @Nested
    class UpdateTests {

        @Test
        void update_whenValidMilk_returnsOkAndUpdatesMilk() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var updateRequest = new MilkRequest(
                    "Updated Milk",
                    savedMilk.getMilkType(),
                    savedMilk.getUpc(),
                    savedMilk.getPrice(),
                    savedMilk.getStock(),
                    Set.of(savedCategory.getId())
            );

            // Act + Assert
            putRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), updateRequest)
                    .expectStatus().isOk()
                    .expectBody(MilkResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedMilk.getId());
                        assertThat(response.name()).isEqualTo("Updated Milk");
                    });

            var updatedMilk = milkRepository.findById(savedMilk.getId()).orElseThrow();
            assertThat(updatedMilk.getName()).isEqualTo("Updated Milk");
        }

        @Test
        void update_whenIdNotExists_returnsNotFound() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var updateRequest = TestDataFactory.milkRequest(savedMilk);

            // Act + Assert
            putRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), updateRequest)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk not found");
                    });
        }

        @Test
        void update_whenInvalidData_returnsBadRequest() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var invalidRequest = new MilkRequest(
                    null,
                    null,
                    "",
                    null,
                    -1,
                    Set.of()
            );

            // Act + Assert
            putRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), invalidRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.name").exists()
                    .jsonPath("$.errors.milkType").exists()
                    .jsonPath("$.errors.upc").exists();
        }

        @Test
        void update_whenUpcDuplicated_returnsConflict() {
            // Arrange
            var milks = dataFactory.persistTwoMilks(savedCategory);
            var milkToUpdate = milks.getFirst();
            var existingUpc = milks.getLast().getUpc();

            var updateRequest = new MilkRequest(
                    milkToUpdate.getName(),
                    milkToUpdate.getMilkType(),
                    existingUpc,
                    milkToUpdate.getPrice(),
                    milkToUpdate.getStock(),
                    Set.of(savedCategory.getId())
            );

            // Act + Assert
            putRequest(ApiPaths.MILKS + "/" + milkToUpdate.getId(), updateRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Milk UPC already exists");
                    });
        }
    }

    @Nested
    class PatchTests {

        @Test
        void patch_whenValidMilk_returnsOkAndUpdatesMilk() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var patchRequest = new MilkPatchRequest("Patched Milk", null, null, null, null, null);

            // Act + Assert
            patchRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), patchRequest)
                    .expectStatus().isOk()
                    .expectBody(MilkResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedMilk.getId());
                        assertThat(response.name()).isEqualTo("Patched Milk");
                    });

            var updatedMilk = milkRepository.findById(savedMilk.getId()).orElseThrow();
            assertThat(updatedMilk.getName()).isEqualTo("Patched Milk");
        }

        @Test
        void patch_whenInvalidUpc_returnsBadRequest() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var patchRequest = TestDataFactory.milkPatchRequestWithInvalidUpc();

            // Act + Assert
            patchRequest(ApiPaths.MILKS + "/" + savedMilk.getId(), patchRequest)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.upc").exists();
        }

        @Test
        void patch_whenIdNotExists_returnsNotFound() {
            // Arrange
            var patchRequest = new MilkPatchRequest("Patched Milk", null, null, null, null, null);

            // Act + Assert
            patchRequest(ApiPaths.MILKS + "/" + UUID.randomUUID(), patchRequest)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk not found");
                    });
        }

        @Test
        void patch_whenUpcDuplicated_returnsConflict() {
            // Arrange
            var milks = dataFactory.persistTwoMilks(savedCategory);
            var milkToPatch = milks.getFirst();
            var existingUpc = milks.getLast().getUpc();

            var patchRequest = new MilkPatchRequest(null, null, existingUpc, null, null, null);

            // Act + Assert
            patchRequest(ApiPaths.MILKS + "/" + milkToPatch.getId(), patchRequest)
                    .expectStatus().isEqualTo(HttpStatus.CONFLICT)
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(409);
                        assertThat(error.message()).contains("Milk UPC already exists");
                    });
        }
    }


    @Nested
    class DeleteTests {

        @Test
        void delete_whenIdExists_returnsNoContent() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);

            // Act + Assert
            deleteRequest(ApiPaths.MILKS + "/" + savedMilk.getId())
                    .expectStatus().isNoContent();

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

    @Nested
    class AuthTests {

        @Test
        void requestWithAuth_returnsOk() {
            // Act + Assert
            getRequest(ApiPaths.MILKS)
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
                    .uri(ApiPaths.MILKS)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
}
