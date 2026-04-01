package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.testdata.IntegrationTestDataFactory;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("integration-test")
@AutoConfigureWebTestClient
public class MilkOrderIT extends AbstractJwtMockIntegrationTest {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    IntegrationTestDataFactory dataFactory;

    @Autowired
    EntityManager entityManager;

    @Autowired
    CategoryRepository categoryRepository;

    Category savedCategory;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        savedCategory = categoryRepository.saveAndFlush(Category.createCategory(UUID.randomUUID().toString()));
    }

    // ---------------
    //      CREATE
    // ---------------

    @Test
    void create_whenValidData_returnsCreated() {
        // Arrange
        var savedCustomer = dataFactory.persistCustomer();
        var savedMilk = dataFactory.persistMilk(savedCategory);
        var orderLineCreateRequest = TestDataFactory.getOrderLineCreateRequest(savedMilk.getId());
        var milkOrderCreateRequest = TestDataFactory.getMilkOrderCreateRequest(savedCustomer.getId(), orderLineCreateRequest);

        // Act
        var result = postRequest(ApiPaths.MILK_ORDERS, milkOrderCreateRequest)
                .expectStatus().isCreated()
                .expectHeader().exists(HttpHeaders.LOCATION)
                .expectBody(Void.class)
                .returnResult();

        // Assert
        var location = result.getResponseHeaders().getFirst(HttpHeaders.LOCATION);
        assertThat(location).isNotBlank();
        assertThat(location).contains(ApiPaths.MILK_ORDERS);
    }

    // ---------------
    //      READ
    // ---------------

    @Test
    void getById_whenIdExists_returnsMilkOrder() {
        // Arrange
        var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);

        // Act + Assert
        getRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId())
                .expectStatus().isOk()
                .expectBody(MilkOrderResponse.class)
                .value(response -> {
                    assertThat(response).isNotNull();
                    assertThat(response.id()).isEqualTo(savedMilkOrder.getId());
                    assertThat(response.orderLineIds()).isNotEmpty();
                    assertThat(response.orderLineIds().iterator().next()).isEqualTo(savedMilkOrder.getOrderLines().iterator().next().getId());
                });
    }

    @Test
    void list_whenMilkOrdersExists_returnsDataList() {
        // Arrange
        dataFactory.persistTwoMilkOrders();

        // Act + Assert
        getRequest(ApiPaths.MILK_ORDERS)
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.content").isArray()
                .jsonPath("$.content.length()").isEqualTo(2)
                .jsonPath("$.content[*].id").isNotEmpty()
                .jsonPath("$.content[*].customerRef").isNotEmpty()
                .jsonPath("$.content[*].orderLineIds").isNotEmpty();
    }

}
