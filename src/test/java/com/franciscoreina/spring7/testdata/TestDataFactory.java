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

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Creates test objects (entities/DTOs) without persisting them.
 */
public class TestDataFactory {

    // ---------------
    //     CUSTOMER
    // ---------------

    public static Customer getNewCustomer() {
        return Customer.createCustomer("Customer name", "customer_" + UUID.randomUUID() + "@domain.com");
    }

    public static Customer getNewCustomer(String email) {
        return Customer.createCustomer("Customer name", email);
    }

    public static CustomerRequest getCustomerCreateRequest(Customer customer) {
        return new CustomerRequest(customer.getName(), customer.getEmail());
    }

    public static CustomerRequest getCustomerCreateRequestNullName() {
        return new CustomerRequest(null, "customer_" + UUID.randomUUID() + "@domain.com");
    }

    public static CustomerRequest getCustomerUpdateRequest(Customer customer) {
        return new CustomerRequest(customer.getName(), customer.getEmail());
    }

    public static CustomerPatchRequest getCustomerPatchRequestWithName() {
        return new CustomerPatchRequest("Patch name", null);
    }

    public static CustomerPatchRequest getCustomerPatchRequestInvalidEmail() {
        return new CustomerPatchRequest(null, "Invalid email");
    }

    // ---------------
    //    CATEGORY
    // ---------------

    public static Category getNewCategory() {
        return Category.createCategory("Dairy Products");
    }

    // ---------------
    //      MILK
    // ---------------

    public static Milk getNewMilk(Category category) {
        var newMilk = Milk.createMilk(
                "Milk name",
                MilkType.SEMI_SKIMMED,
                randomUpc(),
                new BigDecimal("1.20"),
                100,
                Set.of(category));
        return newMilk;
    }

    public static Milk getNewMilk(String upc, Category category) {
        var newMilk = Milk.createMilk(
                "Milk name",
                MilkType.SEMI_SKIMMED,
                upc,
                new BigDecimal("1.20"),
                100,
                Set.of(category));
        return newMilk;
    }

    public static MilkRequest getMilkCreateRequest(Milk milk) {
        return new MilkRequest(milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    public static MilkRequest getMilkCreateRequestNullName() {
        return new MilkRequest(null, MilkType.SEMI_SKIMMED, randomUpc(), new BigDecimal("1.20"), 100, Set.of(UUID.randomUUID()));
    }

    public static MilkPatchRequest getMilkPatchRequestInvalidUpc() {
        return new MilkPatchRequest(null, null, TestDataFactory.randomText(55), null, null, Set.of());
    }

    // ---------------
    //      ORDER
    // ---------------

    //tbf
    public static OrderLineCreateRequest getOrderLineCreateRequest(UUID milkId) {
        return new OrderLineCreateRequest(2, milkId);
    }

    public static MilkOrderRequest getMilkOrderCreateRequest(UUID customerId, OrderLineCreateRequest orderLineCreateRequest) {
        return new MilkOrderRequest("1234TDF", customerId, Set.of(orderLineCreateRequest));
    }

// ---------------
//     HELPERS
// ---------------

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
