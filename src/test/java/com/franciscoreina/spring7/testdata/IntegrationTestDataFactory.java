package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Persists test data into the database for integration tests.
 */
@RequiredArgsConstructor
@Component
public class IntegrationTestDataFactory {

    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;

    public Customer persistCustomer() {
        return customerRepository.saveAndFlush(TestDataFactory.newCustomer());
    }

    public List<Customer> persistTwoCustomers() {
        Customer first = customerRepository.save(TestDataFactory.newCustomer());
        Customer second = customerRepository.save(TestDataFactory.newCustomer());
        customerRepository.flush();
        return List.of(first, second);
    }

    public List<Customer> findTwoCustomers() {
        return customerRepository.findAll(PageRequest.of(0, 2)).getContent();
    }

    public Milk persistMilk() {
        return milkRepository.saveAndFlush(TestDataFactory.newMilk());
    }

    public List<Milk> persistTwoMilks() {
        Milk first = milkRepository.save(TestDataFactory.newMilk());
        Milk second = milkRepository.save(TestDataFactory.newMilk());
        milkRepository.flush();
        return List.of(first, second);
    }

    public List<Milk> findTwoMilks() {
        return milkRepository.findAll(PageRequest.of(0, 2)).getContent();
    }
}
