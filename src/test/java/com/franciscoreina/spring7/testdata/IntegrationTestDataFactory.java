package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Persists test data into the database for integration tests.
 */
@RequiredArgsConstructor
@Component
public final class IntegrationTestDataFactory {

    private static final String ORDER_REF_1 = "ORDER-111";
    private static final String ORDER_REF_2 = "ORDER-222";
    private static final String ORDER_REF_3 = "ORDER-333";

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkOrderRepository milkOrderRepository;

    // ---------------
    //    CUSTOMER
    // ---------------

    public Customer persistCustomer() {
        return customerRepository.saveAndFlush(TestDataFactory.newCustomer());
    }

    public List<Customer> persistTwoCustomers() {
        var customer1 = TestDataFactory.newCustomer();
        var customer2 = TestDataFactory.newCustomer();

        return customerRepository.saveAllAndFlush(List.of(customer1, customer2));
    }

    // ---------------
    //    CATEGORY
    // ---------------

    public Category persistCategory() {
        return categoryRepository.saveAndFlush(TestDataFactory.newCategory());
    }

    // ---------------
    //      MILK
    // ---------------

    public Milk persistMilk(Category savedCategory) {
        return milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
    }

    public List<Milk> persistTwoMilks(Category savedCategory) {
        var milk1 = TestDataFactory.newMilk(savedCategory);
        var milk2 = TestDataFactory.newMilk(savedCategory);

        return milkRepository.saveAllAndFlush(List.of(milk1, milk2));
    }

    // ---------------
    //   MILK ORDER
    // ---------------

    public MilkOrder persistMilkOrder(Category savedCategory) {
        var savedCustomer = persistCustomer();
        var savedMilk = persistMilk(savedCategory);

        var milkOrder = createMilkOrder(savedCustomer, ORDER_REF_1, List.of(
                OrderLine.createOrderLine(savedMilk, 2)
        ));

        return milkOrderRepository.saveAndFlush(milkOrder);
    }

    public List<MilkOrder> persistTwoMilkOrders() {
        var savedCustomer = persistCustomer();
        var savedCategory = persistCategory();
        var savedMilk = persistMilk(savedCategory);

        var milkOrder1 = createMilkOrder(savedCustomer, ORDER_REF_2, List.of(
                OrderLine.createOrderLine(savedMilk, 2)
        ));

        var milkOrder2 = createMilkOrder(savedCustomer, ORDER_REF_3, List.of(
                OrderLine.createOrderLine(savedMilk, 3)
        ));

        return milkOrderRepository.saveAllAndFlush(List.of(milkOrder1, milkOrder2));
    }

    // ---------------
    //     HELPERS
    // ---------------

    private MilkOrder createMilkOrder(Customer customer, String customerRef, List<OrderLine> orderLines) {
        var milkOrder = MilkOrder.createMilkOrder(customer, customerRef);
        orderLines.forEach(milkOrder::addOrderLine);
        return milkOrder;
    }
}
