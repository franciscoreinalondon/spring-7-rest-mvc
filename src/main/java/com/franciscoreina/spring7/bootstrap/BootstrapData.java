package com.franciscoreina.spring7.bootstrap;

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
import com.franciscoreina.spring7.services.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Profile("!integration-test")
@RequiredArgsConstructor
@Component
public class BootstrapData implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkOrderRepository milkOrderRepository;
    private final MilkCsvService milkCsvService;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        loadCategoryData();
        loadCustomerData();
        loadMilkData();
        loadOrderData();
        loadCsvData();
    }

    private void loadCategoryData() {
        if (categoryRepository.count() > 0) {
            return;
        }

        categoryRepository.save(Category.createCategory("Description 1"));
        categoryRepository.save(Category.createCategory("Description 2"));
        categoryRepository.save(Category.createCategory("Description 3"));
    }

    private void loadCustomerData() {
        if (customerRepository.count() > 0) {
            return;
        }

        customerRepository.save(Customer.createCustomer("Alice Johnson", "alice.johnson@example.com"));
        customerRepository.save(Customer.createCustomer("Michael Brown", "michael.brown@example.com"));
        customerRepository.save(Customer.createCustomer("Sofia Martinez", "sofia.martinez@example.com"));
    }

    private void loadMilkData() {
        if (milkRepository.count() > 0) {
            return;
        }

        var savedCategory = categoryRepository.findAll().getFirst();

        milkRepository.save(Milk.createMilk(
                "Organic Whole Milk",
                MilkType.WHOLE,
                "111111111111",
                new BigDecimal("1.89"),
                120,
                Set.of(savedCategory)));

        milkRepository.save(Milk.createMilk(
                "Semi-Skimmed Farm Milk",
                MilkType.SEMI_SKIMMED,
                "222222222222",
                new BigDecimal("1.49"),
                80,
                Set.of(savedCategory)));

        milkRepository.save(Milk.createMilk(
                "Lactose Free Skimmed Milk",
                MilkType.SKIMMED,
                "333333333333",
                new BigDecimal("2.10"),
                60,
                Set.of(savedCategory)));
    }

    private void loadOrderData() {
        if (milkOrderRepository.count() > 0) {
            return;
        }

        var customers = customerRepository.findAll();
        var customer1 = customers.get(0);
        var customer2 = customers.get(1);

        var milks = milkRepository.findAll();
        var milk1 = milks.get(0);
        var milk2 = milks.get(1);
        var milk3 = milks.get(2);

        milkOrderRepository.save(
                createMilkOrder(customer1, "1234R", List.of(
                        OrderLine.createOrderLine(milk1, 1),
                        OrderLine.createOrderLine(milk1, 2)
                ))
        );

        milkOrderRepository.save(
                createMilkOrder(customer1, "5678R", List.of(
                        OrderLine.createOrderLine(milk3, 1)
                ))
        );

        milkOrderRepository.save(
                createMilkOrder(customer2, "1357R", List.of(
                        OrderLine.createOrderLine(milk1, 3),
                        OrderLine.createOrderLine(milk2, 1)
                ))
        );
    }

    public MilkOrder createMilkOrder(Customer customer, String customerRef, List<OrderLine> lines) {
        var milkOrder = MilkOrder.createMilkOrder(customer, customerRef);
        lines.forEach(milkOrder::addOrderLine);

        return milkOrder;
    }

    private void loadCsvData() throws FileNotFoundException {
        if (milkRepository.count() >= 10) {
            return;
        }

        var csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");
        var records = milkCsvService.convertCSV(csvFile);

        records.forEach(record -> milkRepository.save(mapToBeer(record)));
    }

    // The mapper logic is mostly academic, since some properties are filled
    // with placeholder values and do not represent realistic domain data.
    private Milk mapToBeer(MilkCsvRecord record) {
        var savedCategory = categoryRepository.findAll().getFirst();
        return Milk.createMilk(
                record.getMilkName(),
                parseMilkType(record.getStyle()),
                record.getRow().toString(),
                BigDecimal.TEN,
                record.getCount(),
                Set.of(savedCategory));
    }

    private MilkType parseMilkType(String style) {
        try {
            return MilkType.valueOf(style.toUpperCase().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown milk type: " + style);
        }
    }
}
