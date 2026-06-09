package com.oasis.FIFAFanWallet.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(
        BigDecimal amount,
        String base,
        String date,
        Map<String, BigDecimal> rates) {
}
