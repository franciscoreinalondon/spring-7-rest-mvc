package com.franciscoreina.spring7.bootstrap;

import com.franciscoreina.spring7.domain.customer.Customer;
import com.franciscoreina.spring7.domain.milk.Milk;
import com.franciscoreina.spring7.domain.milk.MilkType;
import com.franciscoreina.spring7.dto.file.MilkCsvRecord;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import com.franciscoreina.spring7.services.MilkCsvService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.List;

@Profile("!integration-test")
@RequiredArgsConstructor
@Component
public class BootstrapData implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;
    private final MilkCsvService milkCsvService;

    @Transactional
    @Override
    public void run(String... args) throws Exception {
        loadCustomerData();
        loadMilkData();
        loadCsvData();
    }

    private void loadCustomerData() {
        if (customerRepository.count() > 0) {
            return;
        }

        customerRepository.save(Customer.builder()
                .name("Alice Johnson")
                .email("alice.johnson@example.com")
                .build());

        customerRepository.save(Customer.builder()
                .name("Michael Brown")
                .email("michael.brown@example.com")
                .build());

        customerRepository.save(Customer.builder()
                .name("Sofia Martinez")
                .email("sofia.martinez@example.com")
                .build());
    }

    private void loadMilkData() {
        if (milkRepository.count() > 0) {
            return;
        }

        milkRepository.save(Milk.builder()
                .name("Organic Whole Milk")
                .milkType(MilkType.WHOLE)
                .upc("111111111111")
                .price(new BigDecimal("1.89"))
                .stock(120)
                .build());

        milkRepository.save(Milk.builder()
                .name("Semi-Skimmed Farm Milk")
                .milkType(MilkType.SEMI_SKIMMED)
                .upc("222222222222")
                .price(new BigDecimal("1.49"))
                .stock(80)
                .build());

        milkRepository.save(Milk.builder()
                .name("Lactose Free Skimmed Milk")
                .milkType(MilkType.SKIMMED)
                .upc("333333333333")
                .price(new BigDecimal("2.10"))
                .stock(60)
                .build());

    }

    private void loadCsvData() throws FileNotFoundException {
        if (milkRepository.count() >= 10) {
            return;
        }

        File csvFile = ResourceUtils.getFile("classpath:csvdata/milk_dataset.csv");
        List<MilkCsvRecord> records = milkCsvService.convertCSV(csvFile);

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
