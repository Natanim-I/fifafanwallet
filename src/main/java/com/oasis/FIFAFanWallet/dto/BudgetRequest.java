package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.BudgetPeriod;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BudgetRequest(
        @NotBlank(message = "Budget limit is required.")
        @Positive(message = "Budget limit should be greater than 0.")
        @DecimalMin(value = "0.01", message = "Amount must be greater than zero.")
        BigDecimal limitAmount,
        @NotBlank(message = "Budget period is required")
        BudgetPeriod type,
        @NotNull(message = "Start date is required")
        @FutureOrPresent(message = "Budget start should be present or future date.")
        LocalDateTime startDate,
        @NotNull(message = "End date is required.")
        @Future(message = "Budget end should be future date.")
        LocalDateTime endDate
) {
}
