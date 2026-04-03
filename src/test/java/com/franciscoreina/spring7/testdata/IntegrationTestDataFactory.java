package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.csv.dto.MilkCsvRecord;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.csv.service.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Persists test data into the database for integration tests.
 */
@RequiredArgsConstructor
@Component
public class IntegrationTestDataFactory {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkOrderRepository milkOrderRepository;
    private final MilkCsvService milkCsvService;

    public Customer persistCustomer() {
        return customerRepository.saveAndFlush(TestDataFactory.newCustomer());
    }

    public List<Customer> persistTwoCustomers() {
        var savedCustomer1 = customerRepository.save(TestDataFactory.newCustomer());
        var savedCustomer2 = customerRepository.save(TestDataFactory.newCustomer());
        customerRepository.flush();
        return List.of(savedCustomer1, savedCustomer2);
    }

    public List<Customer> findTwoCustomers() {
        return customerRepository.findAll(PageRequest.of(0, 2)).getContent();
    }

    public Milk persistMilk(Category savedCategory) {
        return milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
    }

    public List<Milk> persistTwoMilks(Category savedCategory) {
        var savedMilk1 = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
        var savedMilk2 = milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));
        return List.of(savedMilk1, savedMilk2);
    }

    public void loadMilkCsvDataset(Category savedCategory)  throws IOException {
        var csvFile = new ClassPathResource("csvdata/milk_dataset.csv");

        try (var is = csvFile.getInputStream()) {
            var records = milkCsvService.convertCSV(is);

            records.forEach(record -> milkRepository.save(mapToBeer(record, savedCategory)));
        }
    }

    public MilkOrder persistMilkOrder(Category savedCategory) {
        var savedCustomer = customerRepository.save(TestDataFactory.newCustomer());
        var savedMilk =  milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));

        var newOrderLine = OrderLine.createOrderLine(savedMilk, 2);
        var newMilkOrder = MilkOrder.createMilkOrder(savedCustomer, "1234-1ITDF");

        newMilkOrder.addOrderLine(newOrderLine);

        return milkOrderRepository.saveAndFlush(newMilkOrder);
    }

    public List<MilkOrder> persistTwoMilkOrders() {
        var savedCustomer = customerRepository.saveAndFlush(TestDataFactory.newCustomer());
        var savedCategory = categoryRepository.saveAndFlush(TestDataFactory.newCategory());
        var savedMilk =  milkRepository.saveAndFlush(TestDataFactory.newMilk(savedCategory));

        var newOrderLine1 = OrderLine.createOrderLine(savedMilk, 2);

        var newMilkOrder1 = MilkOrder.createMilkOrder(savedCustomer, "1234-2ITDF");

        newMilkOrder1.addOrderLine(newOrderLine1);
        var savedMilkOrder1 = milkOrderRepository.saveAndFlush(newMilkOrder1);

        var newOrderLine2 = OrderLine.createOrderLine(savedMilk, 3);

        var newMilkOrder2 = MilkOrder.createMilkOrder(savedCustomer, "5678-2ITDF");

        newMilkOrder2.addOrderLine(newOrderLine2);
        var savedMilkOrder2 = milkOrderRepository.saveAndFlush(newMilkOrder2);

        return List.of(savedMilkOrder1, savedMilkOrder2);
    }

    // The mapper logic is mostly academic, since some properties are filled
    // with placeholder values and do not represent realistic domain data.
    private Milk mapToBeer(MilkCsvRecord record, Category savedCategory) {
        return Milk.createMilk(
                record.getMilkName(),
                parseMilkType(record.getStyle()),
                record.getRow().toString(),
                BigDecimal.TEN,
                record.getCount(),
                Set.of(savedCategory)); //tbr
    }

    private MilkType parseMilkType(String style) {
        try {
            return MilkType.valueOf(style.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown milk type: " + style);
        }
    }
}
