package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.BudgetCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentRequest(
        @NotBlank(message = "Wallet id is required.")
        UUID walletId,
        @NotBlank(message = "Payment amount is required")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        BigDecimal amount,
        @NotBlank(message = "Budget category is required.")
        BudgetCategory budgetCategory,
        @NotBlank(message = "Merchant name is required.")
        String merchantName,
        @NotBlank(message = "Description is required.")
        String description
) {}
