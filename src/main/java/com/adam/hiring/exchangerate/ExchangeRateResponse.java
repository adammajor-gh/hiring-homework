package com.adam.hiring.exchangerate;

import com.adam.hiring.shared.enums.Currency;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record ExchangeRateResponse(
        @NotNull(message = "Currency cannot be null")
        Currency base,

        @NotEmpty(message = "Rates map must not be empty")
        Map<String, Double> rates
) {
}
