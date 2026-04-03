package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.request.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dto.request.customer.CustomerRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dto.request.milk.MilkRequest;
import com.franciscoreina.spring7.dto.request.order.MilkOrderRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineCreateRequest;
import com.franciscoreina.spring7.dto.request.order.OrderLineUpdateRequest;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Creates valid test objects (entities and DTOs) without persisting them.
 */
public final class TestDataFactory {

    private static final String DEFAULT_CUSTOMER_NAME = "Customer name";
    private static final String DEFAULT_CATEGORY_DESCRIPTION = "Dairy Products";
    private static final String DEFAULT_MILK_NAME = "Milk name";
    private static final MilkType DEFAULT_MILK_TYPE = MilkType.SEMI_SKIMMED;
    private static final BigDecimal DEFAULT_MILK_PRICE = new BigDecimal("1.20");
    private static final Integer DEFAULT_MILK_STOCK = 100;
    private static final String DEFAULT_PATCH_NAME = "Patch name";
    private static final String DEFAULT_ORDER_REF = "1234TDF";
    private static final Integer DEFAULT_ORDER_LINE_QUANTITY = 2;

    private TestDataFactory() {
    }

    // ---------------
    //     CUSTOMER
    // ---------------

    public static Customer newCustomer() {
        return Customer.createCustomer(DEFAULT_CUSTOMER_NAME, randomEmail());
    }

    public static Customer newCustomer(String email) {
        return Customer.createCustomer("Customer name", email);
    }

    public static CustomerRequest customerRequest(Customer customer) {
        return new CustomerRequest(customer.getName(), customer.getEmail());
    }

    public static CustomerRequest customerRequestWithNullName() {
        return new CustomerRequest(null, randomEmail());
    }

    public static CustomerPatchRequest customerPatchRequestWithName() {
        return new CustomerPatchRequest(DEFAULT_PATCH_NAME, null);
    }

    public static CustomerPatchRequest customerPatchRequestWithInvalidEmail() {
        return new CustomerPatchRequest(null, "Invalid email");
    }

    public static CustomerPatchRequest customerPatchRequestWithBlankName() {
        return new CustomerPatchRequest(" ", null);
    }

    public static CustomerPatchRequest customerPatchRequestWithDuplicatedEmail(String email) {
        return new CustomerPatchRequest(null, email);
    }

    // ---------------
    //    CATEGORY
    // ---------------

    public static Category newCategory() {
        return Category.createCategory(DEFAULT_CATEGORY_DESCRIPTION);
    }

    public static Category newCategory(String description) {
        return Category.createCategory(description);
    }

    // ---------------
    //      MILK
    // ---------------

    public static Milk newMilk(Category category) {
        return Milk.createMilk(
                DEFAULT_MILK_NAME,
                DEFAULT_MILK_TYPE,
                randomUpc(),
                DEFAULT_MILK_PRICE,
                DEFAULT_MILK_STOCK,
                Set.of(category));
    }

    public static Milk newMilk(String upc, Category category) {
        return Milk.createMilk(
                DEFAULT_MILK_NAME,
                DEFAULT_MILK_TYPE,
                upc,
                DEFAULT_MILK_PRICE,
                DEFAULT_MILK_STOCK,
                Set.of(category)
        );
    }

    public static MilkRequest milkRequest(Milk milk) {
        return new MilkRequest(
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock(),
                milk.getCategories().stream()
                        .map(Category::getId)
                        .collect(Collectors.toSet())
        );
    }

    public static MilkRequest milkRequestWithNullName() {
        return new MilkRequest(
                null,
                DEFAULT_MILK_TYPE,
                randomUpc(),
                DEFAULT_MILK_PRICE,
                DEFAULT_MILK_STOCK,
                Set.of(UUID.randomUUID())
        );
    }

    public static MilkPatchRequest milkPatchRequestWithInvalidUpc() {
        return new MilkPatchRequest(
                null,
                null,
                randomText(55),
                null,
                null,
                null
        );
    }

    // ---------------
    //      ORDER
    // ---------------

    public static OrderLineCreateRequest orderLineCreateRequest(UUID milkId) {
        return new OrderLineCreateRequest(DEFAULT_ORDER_LINE_QUANTITY, milkId);
    }

    public static OrderLineUpdateRequest orderLineUpdateRequest(int quantity) {
        return new OrderLineUpdateRequest(quantity);
    }

    public static MilkOrderRequest milkOrderRequest(UUID customerId, OrderLineCreateRequest orderLineCreateRequest) {
        return new MilkOrderRequest(DEFAULT_ORDER_REF, customerId, Set.of(orderLineCreateRequest));
    }

    // ---------------
    //     HELPERS
    // ---------------

    private static String randomEmail() {
        return "customer_" + UUID.randomUUID() + "@test.com";
    }

    private static String randomUpc() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1_000_000_000L, 10_000_000_000L));
    }

    public static String randomText(int length) {
        var sb = new StringBuilder();

        while (sb.length() < length) {
            sb.append(UUID.randomUUID().toString().replace("-", ""));
        }

        return sb.substring(0, length);
    }
}
