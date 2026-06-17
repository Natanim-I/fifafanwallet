package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import com.oasis.FIFAFanWallet.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID paymentId,
        UUID userId,
        UUID walletId,
        UUID transactionId,
        BigDecimal amount,
        BudgetCategory budgetCategory,
        String merchantName,
        String description,
        PaymentStatus status,
        LocalDateTime createdAt
) {
}
