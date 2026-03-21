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
        return Customer.builder().name("Customer name").email("customer_" + UUID.randomUUID() + "@domain.com").build();
    }

    public static Customer getNewCustomer(String email) {
        return Customer.builder().name("Customer name").email(email).build();
    }

    public static Customer getSavedCustomer(Customer customer) {
        return Customer.builder().id(UUID.randomUUID()).version(0).name(customer.getName()).email(customer.getEmail()).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    public static CustomerCreateRequest getCustomerCreateRequest(Customer customer) {
        return new CustomerCreateRequest(customer.getName(), customer.getEmail());
    }

    public static CustomerCreateRequest getCustomerCreateRequestNullName() {
        return new CustomerCreateRequest(null, "customer_" + UUID.randomUUID() + "@domain.com");
    }

    public static CustomerUpdateRequest getCustomerUpdateRequest(Customer customer) {
        return new CustomerUpdateRequest(customer.getName(), customer.getEmail());
    }

    public static CustomerPatchRequest getCustomerPatchRequestWithName() {
        return new CustomerPatchRequest("Patch name", null);
    }

    public static CustomerPatchRequest getCustomerPatchRequestInvalidEmail() {
        return new CustomerPatchRequest(null, "Invalid email");
    }

    public static CustomerResponse getCustomerResponse(Customer customer) {
        return new CustomerResponse(customer.getId(), customer.getVersion(), customer.getName(), customer.getEmail(), customer.getCreatedAt(), customer.getUpdatedAt());
    }

    // ---------------
    //    CATEGORY
    // ---------------

    public static Category getNewCategory() {
        return Category.builder().description("Dairy Products").build();
    }

    public static Category getSavedCategory(Category category) {
        return Category.builder().id(UUID.randomUUID()).version(0).description(category.getDescription()).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    // ---------------
    //      MILK
    // ---------------

    public static Milk getNewMilk(Category category) {
        return Milk.builder().name("Milk name").milkType(MilkType.SEMI_SKIMMED).upc(randomUpc()).price(new BigDecimal("1.20")).stock(100).categories(Set.of(category)).build();
    }

    public static Milk getNewMilk(String upc, Category category) {
        return Milk.builder().name("Milk name").milkType(MilkType.SEMI_SKIMMED).upc(upc).price(new BigDecimal("1.20")).stock(100).categories(Set.of(category)).build();
    }

    public static Milk getSavedMilk(Milk milk) {
        return Milk.builder().id(UUID.randomUUID()).version(0).name(milk.getName()).milkType(milk.getMilkType()).upc(milk.getUpc()).price(milk.getPrice()).stock(milk.getStock()).categories(milk.getCategories()).createdAt(Instant.now()).updatedAt(Instant.now()).build();
    }

    public static MilkCreateRequest getMilkCreateRequest(Milk milk) {
        return new MilkCreateRequest(milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    public static MilkCreateRequest getMilkCreateRequestNullName() {
        return new MilkCreateRequest(null, MilkType.SEMI_SKIMMED, randomUpc(), new BigDecimal("1.20"), 100, Set.of(UUID.randomUUID()));
    }

    public static MilkUpdateRequest getMilkUpdateRequest(Milk milk) {
        return new MilkUpdateRequest(milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    public static MilkPatchRequest getMilkPatchRequestWithName() {
        return new MilkPatchRequest("Patch name", null, null, null, null, Set.of());
    }

    public static MilkPatchRequest getMilkPatchRequestInvalidUpc() {
        return new MilkPatchRequest(null, null, TestDataFactory.randomText(55), null, null, Set.of());
    }

    public static MilkResponse getMilkResponse(Milk milk) {
        return new MilkResponse(milk.getId(), milk.getVersion(), milk.getName(), milk.getMilkType(), milk.getUpc(), milk.getPrice(), milk.getStock(), milk.getCreatedAt(), milk.getUpdatedAt(), milk.getCategories().stream().map(Category::getId).collect(Collectors.toSet()));
    }

    // ---------------
    //      ORDER
    // ---------------


    public static OrderLine getSavedOrderLine(Milk milk) {
        var savedOrderLine = OrderLine.createOrderLine(milk, 2);

        ReflectionTestUtils.setField(savedOrderLine, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedOrderLine, "version", 0);
        ReflectionTestUtils.setField(savedOrderLine, "createdAt", Instant.now());
        ReflectionTestUtils.setField(savedOrderLine, "updatedAt", Instant.now());

        return savedOrderLine;
    }

    public static MilkOrder getSavedMilkOrder(Customer savedCustomer, Set<OrderLine> savedOrderLines) {
        var savedMilkOrder = MilkOrder.createMilkOrder(savedCustomer, UUID.randomUUID().toString());

        ReflectionTestUtils.setField(savedMilkOrder, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(savedMilkOrder, "version", 0);
        ReflectionTestUtils.setField(savedMilkOrder, "createdAt", Instant.now());
        ReflectionTestUtils.setField(savedMilkOrder, "updatedAt", Instant.now());

        ReflectionTestUtils.setField(savedMilkOrder, "paymentAmount",
                savedOrderLines.stream().map(
                        ol -> ol.getMilk().getPrice().multiply(BigDecimal.valueOf(ol.getOrderQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add));

        savedOrderLines.forEach(savedMilkOrder::addOrderLine);
        return savedMilkOrder;
    }

    //tbf
    public static OrderLineCreateRequest getOrderLineCreateRequest(UUID milkId) {
        return new OrderLineCreateRequest(2, milkId, new BigDecimal(0));
    }

    public static MilkOrderCreateRequest getMilkOrderCreateRequest(UUID customerId, OrderLineCreateRequest orderLineCreateRequest) {
        return new MilkOrderCreateRequest("1234TDF".toString(), new BigDecimal("10.00"), customerId, Set.of(orderLineCreateRequest), null);
    }

    public static MilkOrderResponse getMilkOrderResponse(MilkOrder milkOrder) {
        return new MilkOrderResponse(milkOrder.getId(), milkOrder.getVersion(), milkOrder.getCustomerRef(), milkOrder.getPaymentAmount(), milkOrder.getCreatedAt(), milkOrder.getUpdatedAt(), milkOrder.getCustomer().getId(), milkOrder.getOrderLines().stream().map(OrderLine::getId).collect(Collectors.toSet()), milkOrder.getOrderShipment() != null ? milkOrder.getOrderShipment().getId() : null);
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
