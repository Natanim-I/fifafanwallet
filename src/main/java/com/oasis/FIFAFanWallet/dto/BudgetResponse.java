package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.BudgetPeriod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetResponse(
        UUID budgetId,
        BigDecimal limitAmount,
        BigDecimal spentAmount,
        BudgetPeriod type,
        LocalDateTime startDate,
        LocalDateTime endDate
) {}
