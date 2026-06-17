package com.oasis.FIFAFanWallet.model;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID paymentId;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;
    private UUID userId;
    private UUID walletId;
    private BigDecimal amount;
    @Enumerated(EnumType.STRING)
    private BudgetCategory budgetCategory;
    private String merchantName;
    private String description;
    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate(){
        this.createdAt = LocalDateTime.now();
    }
}
