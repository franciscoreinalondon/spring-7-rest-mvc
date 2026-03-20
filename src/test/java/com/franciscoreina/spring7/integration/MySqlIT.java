package com.franciscoreina.spring7.integration;

import com.franciscoreina.spring7.repositories.MilkRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test that verifies the application can run against a real MySQL database
 * using Testcontainers.
 * <p>
 * A temporary MySQL container is started and dynamically injects the container's
 * JDBC connection properties into the Spring context using @DynamicPropertySource.
 * This allows the application to use the containerized database instead of any locally
 * datasource. Spring Boot context loads the "localmysql" profile.
 * <p>
 * Initial data is pre-populated by the BootstrapData class.
 */
@Testcontainers
@SpringBootTest
@ActiveProfiles("localmysql")
public class MySqlIT {

    @Container
    @ServiceConnection
    static MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8.4");

    @Autowired
    MilkRepository milkRepository;

    @Test
    void testListMilks() {
        var milkList = milkRepository.findAll();

        assertThat(milkList.size()).isGreaterThan(0);
    }
}
