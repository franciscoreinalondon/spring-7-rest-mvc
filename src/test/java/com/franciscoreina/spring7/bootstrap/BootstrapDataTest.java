package com.franciscoreina.spring7.bootstrap;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.repositories.OrderLineRepository;
import com.franciscoreina.spring7.services.MilkCsvService;
import com.franciscoreina.spring7.services.MilkCsvServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@Import({JpaConfig.class, MilkCsvServiceImpl.class})
@DataJpaTest
public class BootstrapDataTest {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private MilkOrderRepository milkOrderRepository;

    @Autowired
    private OrderLineRepository orderLineRepository;

    @Autowired
    private MilkCsvService milkCsvService;

    BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(
                customerRepository,
                milkRepository,
                milkOrderRepository,
                milkCsvService
        );
    }

    @Test
    void run() throws Exception {
        bootstrapData.run("");

        assertThat(customerRepository.count()).isEqualTo(3);
        assertThat(milkRepository.count()).isEqualTo(503);
        assertThat(milkOrderRepository.count()).isEqualTo(3);
        assertThat(orderLineRepository.count()).isEqualTo(5);
    }
}
