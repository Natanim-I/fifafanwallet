package com.oasis.FIFAFanWallet.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record WalletResponse(UUID walletId, BigDecimal balance, Currency currency) {
}
