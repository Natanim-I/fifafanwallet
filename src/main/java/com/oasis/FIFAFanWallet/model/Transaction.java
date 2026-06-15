package com.oasis.FIFAFanWallet.model;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.TransactionStatus;
import com.oasis.FIFAFanWallet.enums.TransactionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID transactionId;
    private BigDecimal amount;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;
    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private BudgetCategory category;
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    private LocalDateTime createdAt;
    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
