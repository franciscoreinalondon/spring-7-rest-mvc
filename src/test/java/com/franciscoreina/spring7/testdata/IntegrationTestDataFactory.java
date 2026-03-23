package com.franciscoreina.spring7.testdata;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.dto.file.MilkCsvRecord;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.services.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
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
        var savedMilk1 = milkRepository.saveAndFlush(TestDataFactory.getNewMilk(savedCategory));
        var savedMilk2 = milkRepository.saveAndFlush(TestDataFactory.getNewMilk(savedCategory));
        return List.of(savedMilk1, savedMilk2);
    }

    public void loadMilkCsvDataset() throws FileNotFoundException {
        var csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");
        var records = milkCsvService.convertCSV(csvFile);

        records.forEach(record -> milkRepository.save(mapToBeer(record)));
    }

    public MilkOrder persistMilkOrder() {
        var savedCustomer = customerRepository.save(TestDataFactory.getNewCustomer());
        var savedCategory = categoryRepository.saveAndFlush(TestDataFactory.getNewCategory());
        var savedMilk =  milkRepository.saveAndFlush(TestDataFactory.getNewMilk(savedCategory));

        var newOrderLine = OrderLine.createOrderLine(savedMilk, 2);
        var newMilkOrder = MilkOrder.createMilkOrder(savedCustomer, "1234-1ITDF");

        newMilkOrder.addOrderLine(newOrderLine);

        return milkOrderRepository.saveAndFlush(newMilkOrder);
    }

    public List<MilkOrder> persistTwoMilkOrders() {
        var savedCustomer = customerRepository.saveAndFlush(TestDataFactory.getNewCustomer());
        var savedCategory = categoryRepository.saveAndFlush(TestDataFactory.getNewCategory());
        var savedMilk =  milkRepository.saveAndFlush(TestDataFactory.getNewMilk(savedCategory));

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
    private Milk mapToBeer(MilkCsvRecord record) {
        var savedCategory = categoryRepository.save(TestDataFactory.getNewCategory());
        return Milk.createMilk(
                record.getMilk(),
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
