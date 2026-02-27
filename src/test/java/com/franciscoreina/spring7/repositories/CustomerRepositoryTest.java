package com.franciscoreina.spring7.repositories;

import com.franciscoreina.spring7.config.JpaConfig;
import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.testdata.TestDataFactory;
import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Import(JpaConfig.class)
@DataJpaTest
public class CustomerRepositoryTest {

    @Autowired
    CustomerRepository customerRepository;

    @Autowired
    EntityManager entityManager;

    // ---------------
    //      SAVE
    // ---------------

    @Test
    public void saveCustomer_whenDataIsValid() {
        // Arrange-Act
        Customer saved = customerRepository.saveAndFlush(TestDataFactory.newCustomer());

        // Assert
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVersion()).isGreaterThanOrEqualTo(0);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();

        assertThat(customerRepository.existsById(saved.getId())).isTrue();
    }

    @Test
    public void saveCustomer_throwException_whenDataDuplicated() {
        // Arrange
        Customer customer = TestDataFactory.newCustomer();
        Customer replica = TestDataFactory.newCustomer(customer.getEmail());

        // Act
        customerRepository.saveAndFlush(customer);

        // Assert
        assertThatThrownBy(() -> customerRepository.saveAndFlush(replica))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    public void saveCustomer_throwException_whenNameIsNull() {
        // Arrange
        Customer customer = TestDataFactory.newCustomer();
        customer.setName(null);

        // Act-Assert
        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void saveCustomer_throwException_whenEmailIsNull() {
        // Arrange
        Customer customer = TestDataFactory.newCustomer(null);

        // Act-Assert
        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    public void saveCustomer_throwException_whenEmailIsInvalid() {
        // Arrange
        Customer customer = TestDataFactory.newCustomer("invalidEmail");

        // Act-Assert
        assertThatThrownBy(() -> customerRepository.saveAndFlush(customer))
                .isInstanceOf(ConstraintViolationException.class);
    }

    // ---------------
    //      FIND
    // ---------------

    @Test
    public void findCustomer_whenIdExists() {
        // Arrange
        Customer saved = customerRepository.saveAndFlush(TestDataFactory.newCustomer());
        entityManager.clear();

        // Act
        Optional<Customer> found = customerRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get()).isNotSameAs(saved);

        assertThat(found.get().getId()).isEqualTo(saved.getId());
        assertThat(found.get().getVersion()).isEqualTo(saved.getVersion());
        assertThat(found.get().getName()).isEqualTo(saved.getName());
        assertThat(found.get().getEmail()).isEqualTo(saved.getEmail());
        assertThat(found.get().getCreatedAt()).isNotNull();
        assertThat(found.get().getUpdatedAt()).isNotNull();
    }

    @Test
    public void findCustomer_returnEmpty_whenIdNotExists() {
        // Arrange-Act
        Optional<Customer> found = customerRepository.findById(UUID.randomUUID());

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    public void findAllCustomers_whenExists() {
        // Arrange
        Customer customer1 = TestDataFactory.newCustomer();
        Customer customer2 = TestDataFactory.newCustomer();
        Customer customer3 = TestDataFactory.newCustomer();

        // Act
        customerRepository.saveAndFlush(customer1);
        customerRepository.saveAndFlush(customer2);
        customerRepository.saveAndFlush(customer3);

        // Assert
        assertThat(customerRepository.count()).isEqualTo(3);
    }

    // ---------------
    //      UPDATE
    // ---------------

    @Test
    public void updateCustomer_whenIsModified() {
        // Arrange
        Customer saved = customerRepository.saveAndFlush(TestDataFactory.newCustomer());
        Integer oldVersion = saved.getVersion();

        // Act
        saved.setEmail("new_email@domain.com");
        Customer updated = customerRepository.saveAndFlush(saved);

        // Assert
        assertThat(updated.getVersion()).isGreaterThan(oldVersion);
        assertThat(updated.getEmail()).isEqualTo("new_email@domain.com");
    }

    // ---------------
    //      DELETE
    // ---------------

    @Test
    public void deleteCustomer_whenIdExists() {
        // Arrange
        Customer saved = customerRepository.saveAndFlush(TestDataFactory.newCustomer());

        // Act
        customerRepository.deleteById(saved.getId());

        // Assert
        assertThat(customerRepository.existsById(saved.getId())).isFalse();
        assertThat(customerRepository.count()).isEqualTo(0);
    }
}
