package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.domain.MilkType;
import com.franciscoreina.spring7.dtos.customer.CustomerCreateRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerPatchRequest;
import com.franciscoreina.spring7.dtos.customer.CustomerResponse;
import com.franciscoreina.spring7.dtos.customer.CustomerUpdateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkCreateRequest;
import com.franciscoreina.spring7.dtos.milk.MilkPatchRequest;
import com.franciscoreina.spring7.dtos.milk.MilkResponse;
import com.franciscoreina.spring7.dtos.milk.MilkUpdateRequest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    public static Milk newMilk() {
        return Milk.builder()
                .name("Milk name")
                .milkType(MilkType.SEMI_SKIMMED)
                .upc(randomUpc())
                .price(new BigDecimal("1.20"))
                .stock(100)
                .build();
    }

    public static Milk newMilk(String upc) {
        return Milk.builder()
                .name("Milk name")
                .milkType(MilkType.SEMI_SKIMMED)
                .upc(upc)
                .price(new BigDecimal("1.20"))
                .stock(100)
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
                milk.getStock()
        );
    }

    public static MilkCreateRequest newMilkCreateRequestNullName() {
        return new MilkCreateRequest(
                null,
                MilkType.SEMI_SKIMMED,
                randomUpc(),
                new BigDecimal("1.20"),
                100
        );
    }

    public static MilkUpdateRequest newMilkUpdateRequest(Milk milk) {
        return new MilkUpdateRequest(
                milk.getName(),
                milk.getMilkType(),
                milk.getUpc(),
                milk.getPrice(),
                milk.getStock()
        );
    }

    public static MilkPatchRequest newMilkPatchRequestWithName() {
        return new MilkPatchRequest(
                "Patch name",
                null,
                null,
                null,
                null
        );
    }

    public static MilkPatchRequest newMilkPatchRequestInvalidUpc() {
        return new MilkPatchRequest(
                null,
                null,
                TestDataFactory.randomText(55),
                null,
                null
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
                milk.getUpdatedAt()
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
