package com.example.bankapp.config;

import com.example.bankapp.model.Customer;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final CustomerRepository customerRepository;

    @Override
    public void run(String... args) {
        if (customerRepository.count() == 0) {
            customerRepository.save(Customer.builder()
                    .accountNumber("100001")
                    .fullName("Kamal Perera")
                    .balance(new BigDecimal("50000.00"))
                    .email("kamal@example.com")
                    .build());

            customerRepository.save(Customer.builder()
                    .accountNumber("100002")
                    .fullName("Nimali Silva")
                    .balance(new BigDecimal("25000.00"))
                    .email("nimali@example.com")
                    .build());

            customerRepository.save(Customer.builder()
                    .accountNumber("100003")
                    .fullName("Saman Fernando")
                    .balance(new BigDecimal("10000.00"))
                    .email("saman@example.com")
                    .build());

            // System Bank Account for 1% Transfer Fees
            customerRepository.save(Customer.builder()
                    .accountNumber("999999")
                    .fullName("Central Bank Operations (Fees)")
                    .balance(BigDecimal.ZERO)
                    .email("bank.fees@system.local")
                    .build());
        }
    }
}