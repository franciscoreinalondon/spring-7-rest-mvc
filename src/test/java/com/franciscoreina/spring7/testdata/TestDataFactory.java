package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.domain.MilkType;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

    private static String randomUpc() {
        return String.valueOf(
                ThreadLocalRandom.current()
                        .nextLong(1_000_000_000L, 10_000_000_000L));
    }
}
