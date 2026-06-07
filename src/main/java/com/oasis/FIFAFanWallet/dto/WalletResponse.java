package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.Currency;
import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(UUID walletId, BigDecimal balance, Currency currency) {
}
