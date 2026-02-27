package com.franciscoreina.spring7.bootstrap;

import com.franciscoreina.spring7.domain.Customer;
import com.franciscoreina.spring7.domain.Milk;
import com.franciscoreina.spring7.domain.MilkType;
import com.franciscoreina.spring7.repositories.CustomerRepository;
import com.franciscoreina.spring7.repositories.MilkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class BootstrapData implements CommandLineRunner {

    private final CustomerRepository customerRepository;
    private final MilkRepository milkRepository;

    @Override
    public void run(String... args) throws Exception {
        loadCustomerData();
        loadMilkData();
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
}
