package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

    // Latest transactions ordered by timestamp
    @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC")
    List<BankTransaction> findAllLatest();

    // Transaction history of a specific account (sender or receiver)
    @Query("SELECT t FROM BankTransaction t WHERE t.fromAccount = :acc OR t.toAccount = :acc ORDER BY t.createdAt DESC")
    List<BankTransaction> findHistory(@Param("acc") String accountNumber);

    // Filter transactions by status
    @Query("SELECT t FROM BankTransaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
    List<BankTransaction> findByStatusLatest(@Param("status") TransactionStatus status);

    // Count by status for dashboard analytics
    @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
    long countByStatus(@Param("status") TransactionStatus status);

    // Task 12 Extension 1: Find transfers larger than min amount
    @Query("SELECT t FROM BankTransaction t WHERE t.amount >= :minAmount ORDER BY t.amount DESC")
    List<BankTransaction> findTransfersGreaterThan(@Param("minAmount") BigDecimal minAmount);

    // Task 12 Extension 2: Calculate total SUCCESS amount sent by an account
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM BankTransaction t WHERE t.fromAccount = :acc AND t.status = 'SUCCESS'")
    BigDecimal getTotalAmountSentByAccount(@Param("acc") String accountNumber);
}