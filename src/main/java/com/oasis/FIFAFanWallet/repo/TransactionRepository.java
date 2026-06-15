package com.oasis.FIFAFanWallet.repo;

import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import com.oasis.FIFAFanWallet.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.wallet.user.userId = :userId
        AND (:type IS NULL OR t.type = :type)
        AND (:currency IS NULL OR t.currency = :currency)
        AND (:startDate IS NULL OR t.createdAt >= :startDate)
        AND (:endDate IS NULL OR t.createdAt <= :endDate)
        AND (:amount IS NULL OR t.amount >= :minAmount)
        AND (:amount IS NULL OR t.amount <= :maxAmount)
        ORDER BY t.createdAt
    """)
    List<Transaction> searchTransactions(
            @Param("userId") UUID userId,
            @Param("type") TransactionType type,
            @Param("currency") Currency currency,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("amount") BigDecimal amount
            );
}
