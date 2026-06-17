package com.oasis.FIFAFanWallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record TransactionRequest(
        @NotBlank(message = "Amount is required.")
        @Positive(message = "Amount must be positive")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        BigDecimal amount) {
}
