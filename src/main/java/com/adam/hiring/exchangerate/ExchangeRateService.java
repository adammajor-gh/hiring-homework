package com.adam.hiring.exchangerate;

import com.adam.hiring.shared.enums.Currency;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ExchangeRateService {

    private static final Logger logger = LoggerFactory.getLogger(ExchangeRateService.class);
    private final RestClient restClient;

    public ExchangeRateService(@Value("${exchange-rate-api.url}") String apiUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(apiUrl)
                .build();
    }

    @Retry(name = "exchangeRetry")
    public ExchangeRateResponse getExchangeRates(Currency base) {
        logger.info("Initiating request to Exchange API. Base: {}", base);

        try {
            ExchangeRateResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/v1/exchangeRate")
                            .queryParam("base", base)
                            .build())
                    .retrieve()
                    //TODO: Handle here the 4xx errors
                    .body(ExchangeRateResponse.class);

            logger.info("Successfully fetched rates for {}", base);
            return response;

        } catch (Exception e) {
            logger.warn("Exchange API attempt failed for base {}: {}", base, e.getMessage());
            throw e;
        }
    }
}