package com.adam.hiring.exchangerate;

import com.adam.hiring.shared.enums.Currency;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ExchangeRateServiceIntegrationTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
    }

    @AfterAll
    static void tearDownClass() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @DynamicPropertySource
    static void overrideProperties(DynamicPropertyRegistry registry) {
        registry.add("exchange-rate-api.url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void resetWireMock() {
        wireMockServer.resetAll();
    }

    @Test
    void shouldFetchExchangeRatesSuccessfully() {
        String responseBody = """
                {
                    "base": "USD",
                    "rates": {
                        "EUR": 0.92,
                        "HUF": 365.50
                    }
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/exchangeRate"))
                .withQueryParam("base", equalTo("USD"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        ExchangeRateResponse response = exchangeRateService.getExchangeRates(Currency.USD);

        assertNotNull(response, "Response should not be null");
        assertEquals(Currency.USD, response.base());
        assertEquals(2, response.rates().size());
        assertEquals(new BigDecimal("0.92"), response.rates().get(Currency.EUR));

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/v1/exchangeRate")));
    }

    @Test
    void shouldRetryOnServerErrorsAndEventuallySucceed() {
        String responseBody = """
                {
                    "base": "EUR",
                    "rates": {
                        "USD": 1.08
                    }
                }
                """;

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/exchangeRate"))
                .withQueryParam("base", equalTo("EUR"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs(Scenario.STARTED)
                .willSetStateTo("2nd attempt")
                .willReturn(aResponse().withStatus(503)));

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/exchangeRate"))
                .withQueryParam("base", equalTo("EUR"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("2nd attempt")
                .willSetStateTo("3rd attempt")
                .willReturn(aResponse().withStatus(503)));

        wireMockServer.stubFor(get(urlPathEqualTo("/v1/exchangeRate"))
                .withQueryParam("base", equalTo("EUR"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("3rd attempt")
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseBody)));

        ExchangeRateResponse response = exchangeRateService.getExchangeRates(Currency.EUR);

        assertNotNull(response);
        assertEquals(Currency.EUR, response.base());
        assertEquals(new BigDecimal("1.08"), response.rates().get(Currency.USD));

        wireMockServer.verify(3, getRequestedFor(urlPathEqualTo("/v1/exchangeRate")));
    }

    @Test
    void shouldThrowExceptionOn4xxClientErrors() {
        wireMockServer.stubFor(get(urlPathEqualTo("/v1/exchangeRate"))
                .withQueryParam("base", equalTo("HUF"))
                .willReturn(aResponse()
                        .withStatus(400)
                        .withBody("Invalid Base Currency")));

        Exception exception = assertThrows(RestClientResponseException.class, () ->
                exchangeRateService.getExchangeRates(Currency.HUF)
        );

        assertTrue(exception.getMessage().contains("400 Bad Request"));

        wireMockServer.verify(1, getRequestedFor(urlPathEqualTo("/v1/exchangeRate")));
    }
}