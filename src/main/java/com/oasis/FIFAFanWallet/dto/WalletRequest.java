package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.Currency;
import jakarta.validation.constraints.NotBlank;

public record WalletRequest(
        @NotBlank(message = "Currency is required.")
        Currency currency) {
}
