package com.oasis.FIFAFanWallet.dto;

import com.oasis.FIFAFanWallet.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record WalletRequest(
        @NotBlank(message = "Currency is required.")
        @Pattern(
                regexp = "^[A-Z]{3}$",
                message = "Currency code must be a valid 3-letter ISO code."
        )
        Currency currency) {
}
