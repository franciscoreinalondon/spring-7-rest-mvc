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
import com.franciscoreina.spring7.dto.response.customer.CustomerResponse;
import com.franciscoreina.spring7.dto.response.milk.MilkResponse;
import org.springframework.test.util.ReflectionTestUtils;

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

    // ---------------
    //     CUSTOMER
    // ---------------

    public static Customer getNewCustomer() {
        return Customer.createCustomer("Customer name", "customer_" + UUID.randomUUID() + "@domain.com");
    }

    public static Customer getNewCustomer(String email) {
        return Customer.createCustomer("Customer name", email);
    }

    public static Customer getSavedCustomer(Customer customer) {
         var savedCustomer = Customer.createCustomer(customer.getName(), customer.getEmail());

        ReflectionTestUtils.setField(savedCustomer, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedCustomer, "version", 0);
        ReflectionTestUtils.setField(savedCustomer, "createdAt", Instant.now());
        ReflectionTestUtils.setField(savedCustomer, "updatedAt", Instant.now());

         return savedCustomer;
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

    public static CustomerResponse getCustomerResponse(Customer customer) {
        return new CustomerResponse(customer.getId(),  customer.getCreatedAt(), customer.getName(), customer.getEmail());
    }

    // ---------------
    //    CATEGORY
    // ---------------

    public static Category getNewCategory() {
        return Category.createCategory("Dairy Products");
    }

    public static Category getSavedCategory(Category category) {
        var savedCategory = Category.createCategory(category.getDescription());

        ReflectionTestUtils.setField(savedCategory, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedCategory, "version", 0);
        ReflectionTestUtils.setField(savedCategory, "createdAt", Instant.now());
        ReflectionTestUtils.setField(savedCategory, "updatedAt", Instant.now());

        return savedCategory;
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

    public static Milk getSavedMilk(Milk milk) {
        var savedMilk = Milk.createMilk(
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock(),
                milk.getCategories());

        ReflectionTestUtils.setField(savedMilk, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedMilk, "version", 0);
        ReflectionTestUtils.setField(savedMilk, "createdAt", Instant.now());
        ReflectionTestUtils.setField(savedMilk, "updatedAt", Instant.now());

        return savedMilk;
    }

    public static MilkRequest getMilkCreateRequest(Milk milk) {
        return new MilkRequest(milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    public static MilkRequest getMilkCreateRequestNullName() {
        return new MilkRequest(null, MilkType.SEMI_SKIMMED, randomUpc(), new BigDecimal("1.20"), 100, Set.of(UUID.randomUUID()));
    }

    public static MilkRequest getMilkUpdateRequest(Milk milk) {
        return new MilkRequest(milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    public static MilkPatchRequest getMilkPatchRequestWithName() {
        return new MilkPatchRequest("Patch name", null, null, null, null, Set.of());
    }

    public static MilkPatchRequest getMilkPatchRequestInvalidUpc() {
        return new MilkPatchRequest(null, null, TestDataFactory.randomText(55), null, null, Set.of());
    }

    public static MilkResponse getMilkResponse(Milk milk) {
        return new MilkResponse(milk.getId(), milk.getCreatedAt(), milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    // ---------------
    //      ORDER
    // ---------------

    //tbf
    public static OrderLineCreateRequest getOrderLineCreateRequest(UUID milkId) {
        return new OrderLineCreateRequest(2, milkId);
    }

    public static MilkOrderRequest getMilkOrderCreateRequest(UUID customerId, OrderLineCreateRequest orderLineCreateRequest) {
        return new MilkOrderRequest("1234TDF".toString(), customerId, Set.of(orderLineCreateRequest));
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
