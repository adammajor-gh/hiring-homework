package com.adam.hiring.exchangerate.stub;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import org.springframework.context.annotation.Profile;

import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@Configuration
@Profile("!test")
public class ExchangeRateApiStub {

    @Value("${wiremock.port:8081}")
    private int port;

    @Bean(destroyMethod = "stop")
    public WireMockServer wireMockServer() {
        WireMockServer server = new WireMockServer(WireMockConfiguration.wireMockConfig().port(port));
        server.start();

        String[] currencies = {"USD", "EUR", "HUF"};

        Map<String, String> ratesJson = Map.of(
                "USD", "{\"base\": \"USD\", \"rates\": {\"EUR\": 0.92, \"HUF\": 365.50}}",
                "EUR", "{\"base\": \"EUR\", \"rates\": {\"USD\": 1.08, \"HUF\": 395.20}}",
                "HUF", "{\"base\": \"HUF\", \"rates\": {\"USD\": 0.0027, \"EUR\": 0.0025}}"
        );

        for (String currency : currencies) {
            for (int i = 1; i <= 5; i++) {
                String currentState = (i == 1) ? Scenario.STARTED : currency + "_Request_" + i;
                String nextState = (i == 5) ? Scenario.STARTED : currency + "_Request_" + (i + 1);

                // 70% chance to fail on the first 4 attempts, the 5th attempt will be success.
                boolean shouldFail = (i < 5) && (Math.random() < 0.70);

                if (shouldFail) {
                    server.stubFor(get(urlPathEqualTo("/api/v1/exchangeRate"))
                            .withQueryParam("base", equalTo(currency))
                            .inScenario("Flaky Exchange - " + currency)
                            .whenScenarioStateIs(currentState)
                            .willSetStateTo(nextState)
                            .willReturn(aResponse()
                                    .withStatus(503)
                                    .withBody("Service Temporarily Unavailable")
                                    .withUniformRandomDelay(500, 5000)));
                } else {
                    server.stubFor(get(urlPathEqualTo("/api/v1/exchangeRate"))
                            .withQueryParam("base", equalTo(currency))
                            .inScenario("Flaky Exchange - " + currency)
                            .whenScenarioStateIs(currentState)
                            .willSetStateTo(nextState)
                            .willReturn(aResponse()
                                    .withStatus(200)
                                    .withHeader("Content-Type", "application/json")
                                    .withBody(ratesJson.get(currency))
                                    .withUniformRandomDelay(500, 5000)));
                }
            }
        }
        return server;
    }
}