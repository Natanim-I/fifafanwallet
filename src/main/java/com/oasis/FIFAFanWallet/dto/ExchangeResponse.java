package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.enums.TransactionStatus;
import com.oasis.FIFAFanWallet.enums.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ExchangeResponse(
        UUID id,
        UUID fromWalletId,
        UUID toWalletId,
        Currency fromCurrency,
        Currency toCurrency,
        BigDecimal amount,
        TransactionType type,
        TransactionStatus status,
        LocalDateTime createdAt
) {
}
