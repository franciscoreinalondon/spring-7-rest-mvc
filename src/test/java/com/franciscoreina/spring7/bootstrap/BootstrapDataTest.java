package com.franciscoreina.spring7.bootstrap;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.csv.service.MilkCsvService;
import com.franciscoreina.spring7.csv.service.MilkCsvServiceImpl;
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
    private CategoryRepository categoryRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private MilkRepository milkRepository;

    @Autowired
    private MilkOrderRepository milkOrderRepository;

    @Autowired
    private MilkCsvService milkCsvService;

    private BootstrapData bootstrapData;

    @BeforeEach
    void setUp() {
        bootstrapData = new BootstrapData(
                categoryRepository,
                customerRepository,
                milkRepository,
                milkOrderRepository,
                milkCsvService
        );
    }

    @Test
    void run_shouldLoadInitialData_whenDatabaseIsEmpty() throws Exception {
        // Act
        bootstrapData.run();

        // Assert
        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(customerRepository.count()).isEqualTo(3);
        assertThat(milkRepository.count()).isEqualTo(503);
        assertThat(milkOrderRepository.count()).isEqualTo(3);

        var orders = milkOrderRepository.findAll();
        assertThat(orders)
                .hasSize(3)
                .allSatisfy(order -> assertThat(order.getOrderLines()).isNotEmpty());
    }

    @Test
    void run_shouldNotDuplicateData_whenExecutedTwice() throws Exception {
        // Act
        bootstrapData.run();
        bootstrapData.run();

        // Assert
        assertThat(categoryRepository.count()).isEqualTo(3);
        assertThat(customerRepository.count()).isEqualTo(3);
        assertThat(milkRepository.count()).isEqualTo(503);
        assertThat(milkOrderRepository.count()).isEqualTo(3);
    }
}
