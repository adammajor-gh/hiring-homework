package com.adam.hiring.exchangerate;

import com.adam.hiring.shared.enums.Currency;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/exchangeRate")

//I only used this controller for testing the exchange rate stub

public class ExchangeRateController {

    private final ExchangeRateService exchangeRateService;

    public ExchangeRateController(ExchangeRateService exchangeRateService) {
        this.exchangeRateService = exchangeRateService;
    }

    @GetMapping("/{base}")
    public ResponseEntity<ExchangeRateResponse> getRatesByPath(@PathVariable Currency base) {
        ExchangeRateResponse rates = exchangeRateService.getExchangeRates(base);
        return ResponseEntity.ok(rates);
    }
}