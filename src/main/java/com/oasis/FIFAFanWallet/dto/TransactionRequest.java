package com.oasis.FIFAFanWallet.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @Positive(message = "Amount must be positive")
        BigDecimal amount) {
}
