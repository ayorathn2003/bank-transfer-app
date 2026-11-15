package com.example.bankapp.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    // Use BigDecimal with precision 14, scale 2 for money to avoid floating-point errors
    @Column(name = "balance", nullable = false, precision = 14, scale = 2)
    private BigDecimal balance;

    @Column(name = "email", unique = true, nullable = false)
    private String email;
}