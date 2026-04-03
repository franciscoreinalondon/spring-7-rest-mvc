package com.franciscoreina.spring7.bootstrap;

import com.franciscoreina.spring7.csv.dto.MilkCsvRecord;
import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Category;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.domain.order.MilkOrder;
import com.franciscoreina.spring7.domain.order.OrderLine;
import com.franciscoreina.spring7.repositories.CategoryRepository;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkOrderRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.csv.service.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
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

        categoryRepository.saveAll(List.of(
                Category.createCategory("Category 1"),
                Category.createCategory("Category 2"),
                Category.createCategory("Category 3")
        ));
    }

    private void loadCustomerData() {
        if (customerRepository.count() > 0) {
            return;
        }

        customerRepository.saveAll(List.of(
                Customer.createCustomer("John Doe", "john@test.com"),
                Customer.createCustomer("Jane Doe", "jane@test.com"),
                Customer.createCustomer("Johnny Doe", "johnny@test.com")
        ));
    }

    private void loadMilkData() {
        if (milkRepository.count() > 0) {
            return;
        }

        var defaultCategory = findDefaultCategory();

        milkRepository.saveAll(List.of(
                Milk.createMilk(
                        "Organic Whole Milk",
                        MilkType.WHOLE,
                        "111111111111",
                        new BigDecimal("1.50"),
                        60,
                        Set.of(defaultCategory)
                ),
                Milk.createMilk(
                        "Semi-Skimmed Farm Milk",
                        MilkType.SEMI_SKIMMED,
                        "222222222222",
                        new BigDecimal("1.75"),
                        100,
                        Set.of(defaultCategory)
                ),
                Milk.createMilk(
                        "Lactose Free Skimmed Milk",
                        MilkType.SKIMMED,
                        "333333333333",
                        new BigDecimal("2.10"),
                        120,
                        Set.of(defaultCategory)
                )
        ));
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

        milkOrderRepository.saveAll(List.of(
                createMilkOrder(customer1, "1234R", List.of(
                        OrderLine.createOrderLine(milk1, 1),
                        OrderLine.createOrderLine(milk2, 2)
                )),
                createMilkOrder(customer1, "5678R", List.of(
                        OrderLine.createOrderLine(milk3, 1)
                )),
                createMilkOrder(customer2, "1357R", List.of(
                        OrderLine.createOrderLine(milk1, 3),
                        OrderLine.createOrderLine(milk2, 1)
                ))
        ));
    }

    private MilkOrder createMilkOrder(Customer customer, String customerRef, List<OrderLine> lines) {
        var milkOrder = MilkOrder.createMilkOrder(customer, customerRef);
        lines.forEach(milkOrder::addOrderLine);

        return milkOrder;
    }

    private void loadCsvData() throws IOException {
        // If CSV data was already imported, do nothing.
        // We keep the manual 3 milks plus CSV import separate.
        if (milkRepository.count() > 3) {
            return;
        }

        var resource = new ClassPathResource("csvdata/milk_dataset.csv");

        try (var is = resource.getInputStream()) {
            var records = milkCsvService.convertCSV(is);
            var defaultCategory = findDefaultCategory();

            var milks = records.stream()
                    .map(record -> mapToMilk(record, defaultCategory))
                    .toList();

            milkRepository.saveAll(milks);
        }
    }

    private Milk mapToMilk(MilkCsvRecord record, Category category) {
        record.normalize();

        return Milk.createMilk(
                record.getMilkName(),
                record.toMilkType(),
                record.getExternalId().toString(),
                buildPrice(record),
                buildStock(record),
                Set.of(category)
        );
    }

    private BigDecimal buildPrice(MilkCsvRecord record) {
        if (record.getMilkFatPercentage() == null) {
            return BigDecimal.ONE;
        }

        // Placeholder pricing strategy for bootstrap/demo data
        return record.getMilkFatPercentage()
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private Integer buildStock(MilkCsvRecord record) {
        var count = record.getProductCount();

        // Default stock for demo purposes
        return (count == null || count <= 0) ? 1 : count;
    }

    private Category findDefaultCategory() {
        return categoryRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("At least one category is required for bootstrap data"));
    }
}
