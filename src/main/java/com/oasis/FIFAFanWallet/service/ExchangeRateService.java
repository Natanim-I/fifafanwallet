package com.oasis.FIFAFanWallet.service;

import com.oasis.FIFAFanWallet.dto.ExchangeRateResponse;
import com.oasis.FIFAFanWallet.enums.Currency;
import com.oasis.FIFAFanWallet.exception.CurrencyExchangeException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.math.BigDecimal;

@Service
public class ExchangeRateService {

    private final RestClient restClient;

    public ExchangeRateService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public BigDecimal getCurrencyExchangeRate(Currency baseCurrency, Currency symbolCurrency){
        String url = "https://api.frankfurter.dev/v1/latest?base=" + baseCurrency.name() + "&symbols=" + symbolCurrency.name();

        ExchangeRateResponse response = restClient.get().uri(url).retrieve().body(ExchangeRateResponse.class);

        if(response == null || response.rates() == null || !response.rates().containsKey(symbolCurrency.name())){
            throw new CurrencyExchangeException("Unable to retrieve exchange rate.");
        }
        return response.rates().get(symbolCurrency.name());
    }
}
