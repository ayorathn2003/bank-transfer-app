package com.example.bankapp.repository;

import com.example.bankapp.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // 1. Find customer by account number
    @Query("SELECT c FROM Customer c WHERE c.accountNumber = :acc")
    Optional<Customer> getByAccountNumber(@Param("acc") String accountNumber);

    // 2. Existence checks
    @Query("SELECT COUNT(c) FROM Customer c WHERE c.accountNumber = :acc")
    long countAccounts(@Param("acc") String accountNumber);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.email = :email")
    long countEmails(@Param("email") String email);

    // 3. Sorted customer list
    @Query("SELECT c FROM Customer c ORDER BY c.fullName ASC")
    List<Customer> findAllOrdered();

    // 4. Case-insensitive search by name
    @Query("SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Customer> searchByName(@Param("keyword") String keyword);

    // 5. Aggregate sum of all balances
    @Query("SELECT SUM(c.balance) FROM Customer c")
    BigDecimal getTotalBalance();

    // 6. Native SQL query count
    @Query(value = "SELECT COUNT(*) FROM customers", nativeQuery = true)
    long countCustomersNative();

    // 7. Atomic Debit: Deduct amount only if balance >= amount
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.balance = c.balance - :amount WHERE c.accountNumber = :acc AND c.balance >= :amount")
    int debit(@Param("acc") String accountNumber, @Param("amount") BigDecimal amount);

    // 8. Atomic Credit: Add amount to account balance
    @Modifying(clearAutomatically = true)
    @Query("UPDATE Customer c SET c.balance = c.balance + :amount WHERE c.accountNumber = :acc")
    int credit(@Param("acc") String accountNumber, @Param("amount") BigDecimal amount);
}