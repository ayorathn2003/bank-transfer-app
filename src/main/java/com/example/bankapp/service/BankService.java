package com.example.bankapp.service;

import com.example.bankapp.exception.AccountNotFoundException;
import com.example.bankapp.exception.BankException;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.Customer;
import com.example.bankapp.model.TransactionStatus;
import com.example.bankapp.repository.BankTransactionRepository;
import com.example.bankapp.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(propagation = Propagation.SUPPORTS)
public class BankService {

    private final CustomerRepository customerRepository;
    private final BankTransactionRepository transactionRepository;
    private final TransactionAuditService auditService;

    public static final String BANK_FEE_ACCOUNT = "999999";

    @Transactional(rollbackFor = Exception.class)
    public BankTransaction transfer(String fromAccount, String toAccount, BigDecimal amount, boolean simulateFailure) {
        try {
            // 1. Validation
            if (fromAccount.equals(toAccount)) {
                throw new BankException("Sender and receiver accounts must be different.");
            }
            if (customerRepository.countAccounts(fromAccount) == 0) {
                throw new AccountNotFoundException("Sender account " + fromAccount + " does not exist.");
            }
            if (customerRepository.countAccounts(toAccount) == 0) {
                throw new AccountNotFoundException("Receiver account " + toAccount + " does not exist.");
            }

            // Task 12 Extension 4: Calculate 1% transfer fee
            BigDecimal fee = amount.multiply(new BigDecimal("0.01")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalDeduction = amount.add(fee);

            // 2. Atomic Debit of Amount + 1% Fee from sender
            int rowsUpdated = customerRepository.debit(fromAccount, totalDeduction);
            if (rowsUpdated == 0) {
                throw new InsufficientBalanceException("Insufficient balance in account " + fromAccount + " (including 1% fee)");
            }

            // 3. Simulated Crash Checkpoint
            if (simulateFailure) {
                throw new BankException("Simulated system failure after withdrawal!");
            }

            // 4. Credit primary amount to receiver & fee to system account 999999
            customerRepository.credit(toAccount, amount);
            if (customerRepository.countAccounts(BANK_FEE_ACCOUNT) > 0) {
                customerRepository.credit(BANK_FEE_ACCOUNT, fee);
            }

            // 5. Record SUCCESS
            BankTransaction successTx = BankTransaction.builder()
                    .fromAccount(fromAccount)
                    .toAccount(toAccount)
                    .amount(amount)
                    .status(TransactionStatus.SUCCESS)
                    .description("Transfer completed successfully (Fee: LKR " + fee + ")")
                    .build();
            return transactionRepository.save(successTx);

        } catch (Exception ex) {
            // Log failure independently in a REQUIRES_NEW transaction before throwing exception to trigger rollback
            auditService.recordFailure(fromAccount, toAccount, amount, ex.getMessage());
            throw ex;
        }
    }

    // Task 12 Extension 3: Deposit Operation
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction deposit(String accountNumber, BigDecimal amount) {
        if (customerRepository.countAccounts(accountNumber) == 0) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found.");
        }
        customerRepository.credit(accountNumber, amount);
        BankTransaction tx = BankTransaction.builder()
                .fromAccount("CASH_DEPOSIT")
                .toAccount(accountNumber)
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description("Direct Cash Deposit")
                .build();
        return transactionRepository.save(tx);
    }

    // Task 12 Extension 3: Withdraw Operation
    @Transactional(rollbackFor = Exception.class)
    public BankTransaction withdraw(String accountNumber, BigDecimal amount) {
        if (customerRepository.countAccounts(accountNumber) == 0) {
            throw new AccountNotFoundException("Account " + accountNumber + " not found.");
        }
        int rows = customerRepository.debit(accountNumber, amount);
        if (rows == 0) {
            throw new InsufficientBalanceException("Insufficient balance for withdrawal in account " + accountNumber);
        }
        BankTransaction tx = BankTransaction.builder()
                .fromAccount(accountNumber)
                .toAccount("CASH_WITHDRAWAL")
                .amount(amount)
                .status(TransactionStatus.SUCCESS)
                .description("Direct Cash Withdrawal")
                .build();
        return transactionRepository.save(tx);
    }

    @Transactional(rollbackFor = Exception.class)
    public Customer createCustomer(Customer customer) {
        if (customerRepository.countAccounts(customer.getAccountNumber()) > 0) {
            throw new BankException("Account number " + customer.getAccountNumber() + " already exists.");
        }
        return customerRepository.save(customer);
    }

    public List<Customer> getAllCustomers(String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return customerRepository.searchByName(keyword.trim());
        }
        return customerRepository.findAllOrdered();
    }

    public BigDecimal getTotalBalance() {
        BigDecimal sum = customerRepository.getTotalBalance();
        return sum != null ? sum : BigDecimal.ZERO;
    }

    public List<BankTransaction> getLatestTransactions() {
        return transactionRepository.findAllLatest();
    }

    public List<BankTransaction> getAccountHistory(String accountNumber) {
        return transactionRepository.findHistory(accountNumber);
    }

    public BigDecimal getTotalSent(String accountNumber) {
        return transactionRepository.getTotalAmountSentByAccount(accountNumber);
    }

    public List<BankTransaction> getLargeTransfers(BigDecimal minAmount) {
        return transactionRepository.findTransfersGreaterThan(minAmount);
    }
}