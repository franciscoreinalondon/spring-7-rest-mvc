package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.api.ApiPaths;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;
import com.franciscoreina.spring7.dto.response.order.OrderLineResponse;
import com.franciscoreina.spring7.exceptions.ApiError;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.repositories.OrderLineRepository;
import com.franciscoreina.spring7.testdata.IntegrationTestDataFactory;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MilkOrderIT extends AbstractJwtMockIntegrationTest {

    @Autowired
    private IntegrationTestDataFactory dataFactory;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MilkOrderRepository milkOrderRepository;

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    private Category savedCategory;

    @BeforeEach
    @Override
    void setUp() {
        super.setUp();
        milkOrderRepository.deleteAll();
        milkRepository.deleteAll();
        customerRepository.deleteAll();
        categoryRepository.deleteAll();

        savedCategory = categoryRepository.saveAndFlush(
                Category.createCategory(UUID.randomUUID().toString())
        );
    }

    @Nested
    class CreateTests {

        @Test
        void create_whenValidData_returnsCreatedAndPersistsMilkOrder() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var orderLineCreateRequest = TestDataFactory.orderLineCreateRequest(savedMilk.getId());
            var milkOrderCreateRequest = TestDataFactory.milkOrderRequest(savedCustomer.getId(), orderLineCreateRequest);

            // Act + Assert
            var response = postRequest(ApiPaths.MILK_ORDERS, milkOrderCreateRequest)
                    .expectStatus().isCreated()
                    .expectHeader().exists(HttpHeaders.LOCATION)
                    .expectBody(MilkOrderResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();
            assertThat(response.customerRef()).isEqualTo(milkOrderCreateRequest.customerRef());
            assertThat(response.customerId()).isEqualTo(savedCustomer.getId());
            assertThat(response.orderLines()).hasSize(1);
            assertThat(milkOrderRepository.existsById(response.id())).isTrue();
        }

        @Test
        void create_whenCustomerIdIsNull_returnsBadRequest() {
            // Arrange
            var request = new MilkOrderRequest(
                    "ORDER-123",
                    null,
                    Set.of(TestDataFactory.orderLineCreateRequest(UUID.randomUUID()))
            );

            // Act + Assert
            postRequest(ApiPaths.MILK_ORDERS, request)
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.customerId").exists();
        }

        @Test
        void create_whenMilkDoesNotExist_returnsNotFound() {
            // Arrange
            var savedCustomer = dataFactory.persistCustomer();
            var request = TestDataFactory.milkOrderRequest(
                    savedCustomer.getId(),
                    TestDataFactory.orderLineCreateRequest(UUID.randomUUID())
            );

            // Act + Assert
            postRequest(ApiPaths.MILK_ORDERS, request)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk not found");
                    });
        }

        @Test
        void create_whenCustomerDoesNotExist_returnsNotFound() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var request = TestDataFactory.milkOrderRequest(
                    UUID.randomUUID(),
                    TestDataFactory.orderLineCreateRequest(savedMilk.getId())
            );

            // Act + Assert
            postRequest(ApiPaths.MILK_ORDERS, request)
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
    class ReadTests {

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
                        assertThat(response.customerRef()).isEqualTo(savedMilkOrder.getCustomerRef());
                        assertThat(response.customerId()).isEqualTo(savedMilkOrder.getCustomer().getId());
                        assertThat(response.orderLines()).isNotEmpty();
                        assertThat(response.orderLines().iterator().next().id())
                                .isEqualTo(savedMilkOrder.getOrderLines().iterator().next().getId());
                    });
        }

        @Test
        void getById_whenIdNotExists_returnsNotFound() {
            // Act + Assert
            getRequest(ApiPaths.MILK_ORDERS + "/" + UUID.randomUUID())
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk order not found");
                    });
        }

        @Test
        void search_whenMilkOrdersExist_returnsDataList() {
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
                    .jsonPath("$.content[*].orderLines").isNotEmpty();
        }

        @Test
        void search_whenMilkOrdersDoNotExist_returnsEmptyPage() {
            // Act + Assert
            getRequest(ApiPaths.MILK_ORDERS)
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.content").isArray()
                    .jsonPath("$.content.length()").isEqualTo(0);
        }
    }

    @Nested
    class OrderLineTests {

        @Test
        void addLine_whenValidData_returnsCreatedAndPersistsLine() {
            // Arrange
            var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);
            var anotherMilk = dataFactory.persistMilk(savedCategory);
            var request = TestDataFactory.orderLineCreateRequest(anotherMilk.getId());

            // Act + Assert
            var response = postRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES, request)
                    .expectStatus().isCreated()
                    .expectBody(OrderLineResponse.class)
                    .returnResult()
                    .getResponseBody();

            assertThat(response).isNotNull();
            assertThat(response.id()).isNotNull();
            assertThat(response.milkOrderId()).isEqualTo(savedMilkOrder.getId());
            assertThat(response.milkId()).isEqualTo(anotherMilk.getId());
            assertThat(orderLineRepository.countByMilkOrderId(savedMilkOrder.getId())).isEqualTo(2);
        }

        @Test
        void addLine_whenMilkOrderDoesNotExist_returnsNotFound() {
            // Arrange
            var savedMilk = dataFactory.persistMilk(savedCategory);
            var request = TestDataFactory.orderLineCreateRequest(savedMilk.getId());

            // Act + Assert
            postRequest(ApiPaths.MILK_ORDERS + "/" + UUID.randomUUID() + ApiPaths.ORDER_LINES, request)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk order not found");
                    });
        }

        @Test
        void addLine_whenMilkDoesNotExist_returnsNotFound() {
            // Arrange
            var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);
            var request = TestDataFactory.orderLineCreateRequest(UUID.randomUUID());

            // Act + Assert
            postRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES, request)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk not found");
                    });
        }

        @Test
        void updateLine_whenValidData_returnsOkAndUpdatesQuantity() {
            // Arrange
            var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);
            var savedOrderLine = savedMilkOrder.getOrderLines().iterator().next();
            var request = TestDataFactory.orderLineUpdateRequest(5);

            // Act + Assert
            putRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES + "/" + savedOrderLine.getId(), request)
                    .expectStatus().isOk()
                    .expectBody(OrderLineResponse.class)
                    .value(response -> {
                        assertThat(response).isNotNull();
                        assertThat(response.id()).isEqualTo(savedOrderLine.getId());
                        assertThat(response.requestedQuantity()).isEqualTo(5);
                    });

            var updatedLine = orderLineRepository.findById(savedOrderLine.getId()).orElseThrow();
            assertThat(updatedLine.getRequestedQuantity()).isEqualTo(5);
        }

        @Test
        void updateLine_whenMilkOrderDoesNotExist_returnsNotFound() {
            // Arrange
            var request = TestDataFactory.orderLineUpdateRequest(5);

            // Act + Assert
            putRequest(ApiPaths.MILK_ORDERS + "/" + UUID.randomUUID() + ApiPaths.ORDER_LINES + "/" + UUID.randomUUID(), request)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk order not found");
                    });
        }

        @Test
        void updateLine_whenOrderLineDoesNotExist_returnsNotFound() {
            // Arrange
            var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);
            var request = TestDataFactory.orderLineUpdateRequest(5);

            // Act + Assert
            putRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES + "/" + UUID.randomUUID(), request)
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Order line not found");
                    });
        }

        @Test
        void removeLine_whenValidData_returnsNoContent() {
            // Arrange
            var savedMilkOrder = dataFactory.persistTwoMilkOrders().getFirst();
            var savedOrderLine = savedMilkOrder.getOrderLines().iterator().next();

            // Act + Assert
            deleteRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES + "/" + savedOrderLine.getId())
                    .expectStatus().isNoContent();

            var exists = orderLineRepository.existsById(savedOrderLine.getId());
            assertThat(exists).isFalse();
        }

        @Test
        void removeLine_whenMilkOrderDoesNotExist_returnsNotFound() {
            // Act + Assert
            deleteRequest(ApiPaths.MILK_ORDERS + "/" + UUID.randomUUID() + ApiPaths.ORDER_LINES + "/" + UUID.randomUUID())
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Milk order not found");
                    });
        }

        @Test
        void removeLine_whenOrderLineDoesNotExist_returnsNotFound() {
            // Arrange
            var savedMilkOrder = dataFactory.persistMilkOrder(savedCategory);

            // Act + Assert
            deleteRequest(ApiPaths.MILK_ORDERS + "/" + savedMilkOrder.getId() + ApiPaths.ORDER_LINES + "/" + UUID.randomUUID())
                    .expectStatus().isNotFound()
                    .expectBody(ApiError.class)
                    .value(error -> {
                        assertThat(error).isNotNull();
                        assertThat(error.status()).isEqualTo(404);
                        assertThat(error.message()).contains("Order line not found");
                    });
        }
    }

    @Nested
    class AuthTests {

        @Test
        void requestWithAuth_returnsOk() {
            // Act + Assert
            getRequest(ApiPaths.MILK_ORDERS)
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
                    .uri(ApiPaths.MILK_ORDERS)
                    .exchange()
                    .expectStatus().isUnauthorized();
        }
    }
}
