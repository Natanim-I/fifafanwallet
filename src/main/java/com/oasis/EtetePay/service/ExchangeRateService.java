package com.oasis.EtetePay.service;

import com.oasis.EtetePay.dto.ExchangeRateResponse;
import com.oasis.EtetePay.enums.Currency;
import com.oasis.EtetePay.exception.CurrencyExchangeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

@Service
public class ExchangeRateService {

    private final RestClient restClient;
    @Value("${exchange.api.key}")
    private String apiKey;

    public ExchangeRateService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public BigDecimal getCurrencyExchangeRate(Currency baseCurrency, Currency symbolCurrency){
        String url = "https://v6.exchangerate-api.com/v6/" + apiKey + "/pair/" +baseCurrency.name() + "/" + symbolCurrency.name();

        ExchangeRateResponse response = restClient.get().uri(url).retrieve().body(ExchangeRateResponse.class);

        if(response == null){
            throw new CurrencyExchangeException("Exchange rate provider returned no response.");
        }

        if (!"success".equalsIgnoreCase(response.result())) {
            throw new CurrencyExchangeException("Exchange rate provider returned an unsuccessful response.");
        }

        if (response.conversionRate() == null) {
            throw new CurrencyExchangeException("Exchange rate is missing from the response.");
        }

        if (response.baseCode() == null || response.targetCode() == null) {
            throw new CurrencyExchangeException("Currency information is missing.");
        }
        return response.conversionRate();
    }
}
