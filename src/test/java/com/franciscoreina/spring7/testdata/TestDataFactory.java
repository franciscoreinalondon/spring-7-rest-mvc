package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.request.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkUpdateRequest;
import com.franciscoreina.spring7.dto.request.order.MilkOrderCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import com.franciscoreina.spring7.dto.response.order.MilkOrderResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Creates test objects (entities/DTOs) without persisting them.
 */
public class TestDataFactory {

    public static Customer newCustomer() {
        return Customer.builder()
                .name("Customer name")
                .email("customer_" + UUID.randomUUID() + "@domain.com")
                .build();
    }

    public static Customer newCustomer(String email) {
        return Customer.builder()
                .name("Customer name")
                .email(email)
                .build();
    }

    public static Customer newSavedCustomer(Customer customer) {
        return Customer.builder()
                .id(UUID.randomUUID())
                .version(0)
                .name(customer.getName())
                .email(customer.getEmail())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static CustomerCreateRequest newCustomerCreateRequest(Customer customer) {
        return new CustomerCreateRequest(
                customer.getName(),
                customer.getEmail()
        );
    }

    public static CustomerCreateRequest newCustomerCreateRequestNullName() {
        return new CustomerCreateRequest(
                null,
                "customer_" + UUID.randomUUID() + "@domain.com"
        );
    }

    public static CustomerUpdateRequest newCustomerUpdateRequest(Customer customer) {
        return new CustomerUpdateRequest(
                customer.getName(),
                customer.getEmail()
        );
    }

    public static CustomerPatchRequest newCustomerPatchRequestWithName() {
        return new CustomerPatchRequest(
                "Patch name",
                null
        );
    }

    public static CustomerPatchRequest newCustomerPatchRequestInvalidEmail() {
        return new CustomerPatchRequest(
                null,
                "Invalid email"
        );
    }

    public static CustomerResponse newCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getVersion(),
                customer.getName(),
                customer.getEmail(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }

    public static Category newCategory() {
        return Category.builder()
                .description("Dairy Products")
                .build();
    }

    public static Category newSavedCategory() {
        return Category.builder()
                .id(UUID.randomUUID())
                .version(0)
                .description("Dairy Products")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static Milk newMilk(Category category) {
        return Milk.builder()
                .name("Milk name")
                .milkType(MilkType.SEMI_SKIMMED)
                .upc(randomUpc())
                .price(new BigDecimal("1.20"))
                .stock(100)
                .categories(Set.of(category))
                .build();
    }

    public static Milk newMilk(String upc, Category category) {
        return Milk.builder()
                .name("Milk name")
                .milkType(MilkType.SEMI_SKIMMED)
                .upc(upc)
                .price(new BigDecimal("1.20"))
                .stock(100)
                .categories(Set.of(category))
                .build();
    }

    public static Milk newSavedMilk(Milk milk) {
        return Milk.builder()
                .id(UUID.randomUUID())
                .version(0)
                .name(milk.getName())
                .milkType(milk.getMilkType())
                .upc(milk.getUpc())
                .price(milk.getPrice())
                .stock(milk.getStock())
                .categories(milk.getCategories())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    public static MilkCreateRequest newMilkCreateRequest(Milk milk) {
        return new MilkCreateRequest(
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock(),
                milk.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet())
        );
    }

    public static MilkCreateRequest newMilkCreateRequestNullName() {
        return new MilkCreateRequest(
                null,
                MilkType.SEMI_SKIMMED,
                randomUpc(),
                new BigDecimal("1.20"),
                100,
                Set.of(UUID.randomUUID())
        );
    }

    public static MilkUpdateRequest newMilkUpdateRequest(Milk milk) {
        return new MilkUpdateRequest(
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock(),
                milk.getCategories() == null ? Set.of() :
                        milk.getCategories()
                                .stream()
                                .map(Category::getId)
                                .collect(Collectors.toSet())
        );
    }

    public static MilkPatchRequest newMilkPatchRequestWithName() {
        return new MilkPatchRequest(
                "Patch name",
                null,
                null,
                null,
                null,
                Set.of()
        );
    }

    public static MilkPatchRequest newMilkPatchRequestInvalidUpc() {
        return new MilkPatchRequest(
                null,
                null,
                TestDataFactory.randomText(55),
                null,
                null,
                Set.of()
        );
    }

    public static MilkResponse newMilkResponse(Milk milk) {
        return new MilkResponse(
                milk.getId(),
                milk.getVersion(),
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock(),
                milk.getCreatedAt(),
                milk.getUpdatedAt(),
                milk.getCategories()
                        .stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet())
        );
    }

    public static OrderLineCreateRequest newOrderLineCreateRequest(MilkResponse milkResponse) {
        return new OrderLineCreateRequest(
                2,
                milkResponse.id()
        );
    }

    public static MilkOrderCreateRequest newMilkOrderCreateRequest(
            CustomerResponse customerResponse, OrderLineCreateRequest orderLineCreateRequest) {
        return new MilkOrderCreateRequest(
                "1234r",
                new BigDecimal("10.00"),
                customerResponse.id(),
                Set.of(orderLineCreateRequest)
        );
    }

    public static MilkOrderResponse newMilkOrderResponse(MilkOrder milkOrder) {
        return new MilkOrderResponse(
                milkOrder.getId(),
                milkOrder.getVersion(),
                milkOrder.getCustomerRef(),
                milkOrder.getPaymentAmount(),
                milkOrder.getCreatedAt(),
                milkOrder.getUpdatedAt(),
                milkOrder.getCustomer().getId(),
                milkOrder.getOrderLines().stream()
                        .map(OrderLine::getId)
                        .collect(Collectors.toSet()),
                milkOrder.getOrderShipment().getId()
        );
    }

    private static String randomUpc() {
        return String.valueOf(
                ThreadLocalRandom.current()
                        .nextLong(1_000_000_000L, 10_000_000_000L));
    }

    public static String randomText(int length) {
        StringBuilder sb = new StringBuilder();

        while (sb.length() < length) {
            sb.append(UUID.randomUUID().toString().replace("-", ""));
        }
        return sb.substring(0, length);
    }
}
