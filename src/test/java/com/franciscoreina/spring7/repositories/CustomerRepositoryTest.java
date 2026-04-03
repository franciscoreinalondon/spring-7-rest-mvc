package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.customer.Customer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Pageable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    void save_shouldPersistCustomer_whenDataIsValid() {
        // Arrange
        var customer = Customer.createCustomer("John Doe", "john@test.com");

        // Act
        var saved = customerRepository.saveAndFlush(customer);

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo(customer.getName());
        assertThat(saved.getEmail()).isEqualTo(customer.getEmail());
    }

    @Test
    void save_shouldThrowException_whenEmailIsDuplicated() {
        // Arrange
        customerRepository.saveAndFlush(Customer.createCustomer("John", "same@test.com"));

        var duplicated = Customer.createCustomer("Jane", "same@test.com");

        // Act + Assert
        assertThatThrownBy(() -> customerRepository.saveAndFlush(duplicated))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    // ---------------
    //      FIND
    // ---------------

    @Test
    void findByEmailIgnoreCase_shouldReturnCustomer_whenExistsIgnoringCase() {
        // Arrange
        var saved = customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "john@test.com")
        );

        // Act
        var result = customerRepository.findByEmailIgnoreCase("JOHN@TEST.COM");

        // Assert
        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(saved.getId());
    }

    @Test
    void existsByEmailIgnoreCase_shouldReturnTrue_whenExistsIgnoringCase() {
        // Arrange
        customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "john@test.com")
        );

        // Act
        var exists = customerRepository.existsByEmailIgnoreCase("JOHN@TEST.COM");

        // Assert
        assertThat(exists).isTrue();
    }

    // ---------------
    //      SEARCH
    // ---------------

    @Test
    void findAllByNameContainingIgnoreCase_shouldReturnMatchingCustomers() {
        // Arrange
        var customer1 = customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "john@test.com")
        );
        var customer2 = customerRepository.saveAndFlush(
                Customer.createCustomer("Johnny Doe", "johnny@test.com")
        );

        var pageable = Pageable.ofSize(10);

        // Act
        var result = customerRepository.findAllByNameContainingIgnoreCase("JOHN", pageable);

        // Assert
        assertThat(result.getContent())
                .extracting(Customer::getId)
                .containsExactly(customer1.getId(), customer2.getId());
    }

    @Test
    void findAllByEmailIgnoreCase_shouldReturnMatchingCustomer() {
        // Arrange
        var customer = customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "john@test.com")
        );

        var pageable = Pageable.ofSize(10);

        // Act
        var result = customerRepository.findAllByEmailIgnoreCase("JOHN@TEST.COM", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(customer.getId());
    }

    @Test
    void findAllByNameContainingIgnoreCaseAndEmailIgnoreCase_shouldReturnMatchingCustomer() {
        // Arrange
        var customer = customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "john@test.com")
        );

        customerRepository.saveAndFlush(
                Customer.createCustomer("John Doe", "other@test.com")
        );

        var pageable = Pageable.ofSize(10);

        // Act
        var result = customerRepository
                .findAllByNameContainingIgnoreCaseAndEmailIgnoreCase("john", "john@test.com", pageable);

        // Assert
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getId()).isEqualTo(customer.getId());
    }
}
