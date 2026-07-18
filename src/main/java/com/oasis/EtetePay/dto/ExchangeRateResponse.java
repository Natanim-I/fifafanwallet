package com.oasis.EtetePay.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;
import java.util.Map;

public record ExchangeRateResponse(
        BigDecimal amount,
        String result,
        @JsonProperty("base_code")
        String baseCode,
        @JsonProperty("target_code")
        String targetCode,
        @JsonProperty("conversion_rate")
        BigDecimal conversionRate

) {
}
