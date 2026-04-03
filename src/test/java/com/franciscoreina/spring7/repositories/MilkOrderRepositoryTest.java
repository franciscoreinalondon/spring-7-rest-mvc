package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
class MilkOrderRepositoryTest {

    @Autowired
    private MilkOrderRepository milkOrderRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    void save_shouldPersistMilkOrder_whenDataIsValid() {
        // Arrange
        var order = order(createCustomer(), "ORDER-123");

        // Act
        var saved = milkOrderRepository.saveAndFlush(order);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCustomerRef()).isEqualTo(order.getCustomerRef());
        assertThat(saved.getMilkOrderStatus()).isEqualTo(order.getMilkOrderStatus());
        assertThat(saved.getPaymentAmount()).isEqualTo(order.getPaymentAmount());
        assertThat(saved.getCustomer()).isNotNull();
        assertThat(saved.getOrderLines()).hasSize(1);
    }

    @Test
    void save_shouldThrowException_whenCustomerRefIsDuplicated() {
        // Arrange
        var customer = createCustomer();
        milkOrderRepository.saveAndFlush(order(customer, "ORDER-123"));
        var duplicated = order(customer, "ORDER-123");

        // Act + Assert
        assertThatThrownBy(() -> milkOrderRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------
    //      SEARCH
    // ---------------

    @Test
    void findAllByCustomerRefContainingIgnoreCase_shouldReturnMatchingOrders() {
        // Arrange
        var customer = createCustomer();
        var order1 = milkOrderRepository.saveAndFlush(order(customer, "ORDER-123"));
        var order2 = milkOrderRepository.saveAndFlush(order(customer, "ORDER-456"));
        milkOrderRepository.saveAndFlush(order(customer, "OTHER-999"));

        var pageable = Pageable.ofSize(10);

        // Act
        var result = milkOrderRepository.findAllByCustomerRefContainingIgnoreCase("order", pageable);

        // Assert
        assertThat(result.getContent())
                .extracting(MilkOrder::getId)
                .containsExactly(order1.getId(), order2.getId());
    }

    private Customer createCustomer() {
        return customerRepository.saveAndFlush(Customer.createCustomer("John Doe", "john@test.com"));
    }

    private MilkOrder order(Customer customer, String customerRef) {
        var milk = createMilk();
        var order = MilkOrder.createMilkOrder(customer, customerRef);
        order.addOrderLine(OrderLine.createOrderLine(milk, 2));

        return order;
    }

    private Milk createMilk() {
        var upc = "UPC" + UUID.randomUUID().toString().substring(0, 8);

        return milkRepository.saveAndFlush(
                Milk.createMilk(
                        "Whole Milk",
                        MilkType.WHOLE,
                        upc,
                        new BigDecimal("2.50"),
                        10,
                        Set.of(savedCategory("Category_" + upc))
                )
        );
    }

    private Category savedCategory(String description) {
        return categoryRepository.saveAndFlush(Category.createCategory(description));
    }
}
