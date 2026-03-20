package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.file.MilkCsvRecord;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.services.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

/**
 * Persists test data into the database for integration tests.
 */
@RequiredArgsConstructor
@Component
public class IntegrationTestDataFactory {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkCsvService milkCsvService;

    public Customer persistCustomer() {
        return customerRepository.saveAndFlush(TestDataFactory.getNewCustomer());
    }

    public List<Customer> persistTwoCustomers() {
        var savedCustomer1 = customerRepository.save(TestDataFactory.getNewCustomer());
        var savedCustomer2 = customerRepository.save(TestDataFactory.getNewCustomer());
        customerRepository.flush();
        return List.of(savedCustomer1, savedCustomer2);
    }

    public List<Customer> findTwoCustomers() {
        return customerRepository.findAll(PageRequest.of(0, 2)).getContent();
    }

    public Milk persistMilk() {
        var savedCategory = categoryRepository.saveAndFlush(TestDataFactory.getNewCategory());
        return milkRepository.saveAndFlush(TestDataFactory.getNewMilk(savedCategory));
    }

    public List<Milk> persistTwoMilks() {
        var savedCategory = categoryRepository.saveAndFlush(TestDataFactory.getNewCategory());
        var savedMilk1 = milkRepository.save(TestDataFactory.getNewMilk(savedCategory));
        var savedMilk2 = milkRepository.save(TestDataFactory.getNewMilk(savedCategory));
        milkRepository.flush();
        return List.of(savedMilk1, savedMilk2);
    }

    public List<Milk> findTwoMilks() {
        return milkRepository.findAll(PageRequest.of(0, 2)).getContent();
    }

    public void loadMilkCsvDataset() throws FileNotFoundException {
        var csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");
        var records = milkCsvService.convertCSV(csvFile);

        records.forEach(record -> milkRepository.save(mapToBeer(record)));
    }

    // The mapper logic is mostly academic, since some properties are filled
    // with placeholder values and do not represent realistic domain data.
    private Milk mapToBeer(MilkCsvRecord record) {
        return Milk.builder()
                .name(record.getMilk())
                .milkType(parseMilkType(record.getStyle()))
                .price(BigDecimal.TEN)
                .upc(record.getRow().toString())
                .stock(record.getCount())
                .build();
    }

    private MilkType parseMilkType(String style) {
        try {
            return MilkType.valueOf(style.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown milk type: " + style);
        }
    }
}
