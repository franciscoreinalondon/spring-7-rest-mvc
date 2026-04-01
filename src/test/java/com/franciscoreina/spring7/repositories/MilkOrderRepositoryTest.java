package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderShipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@Import(JpaConfig.class)
@SpringBootTest
class MilkOrderRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    MilkRepository milkRepository;

    @Autowired
    MilkOrderRepository milkOrderRepository;

    Customer customer;
    Milk milk;

    @BeforeEach
    void setUp() {
        customer = customerRepository.findAll().getFirst();
        milk = milkRepository.findAll().getFirst();
    }

    @Transactional
    @Test//TBF
    void testMilkOrder() {
        // Act
        MilkOrder milkOrder = MilkOrder.createMilkOrder(customer, "1234MO");

        milkOrder.addOrderShipment("12345");

//        OrderShipment orderShipment = OrderShipment.createOrderShipment("12345", milkOrder);


//        milkOrder.addOrderShipment(orderShipment);
//        customer.addMilkOrder(milkOrder);

        MilkOrder savedMilkOrder = milkOrderRepository.saveAndFlush(milkOrder);


        // Assert
        assertThat(savedMilkOrder.getOrderShipment()).isNotNull();
        assertThat(savedMilkOrder.getOrderShipment().getMilkOrder()).isNotNull();
//        assertThat(savedMilkOrder.getCustomer().getMilkOrders().contains(milkOrder));
    }
}