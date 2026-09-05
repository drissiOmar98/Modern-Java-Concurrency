package com.omar.loomdemo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

/**
 * Wires up the {@link RestClient} used to call {@code banking-data-service}.
 *
 * <p>Kept separate from the application's main class and from individual
 * services so that HTTP client construction (base URL, timeouts,
 * interceptors, etc.) lives in exactly one place.
 */
@Configuration
public class RestClientConfig {

    /**
     * Creates the shared {@link RestClient} used by all downstream service
     * clients, pre-configured with the banking-data-service base URL.
     *
     * @param restClientBuilder Spring Boot's auto-configured builder
     * @param properties        the downstream service connection settings
     * @return a {@link RestClient} bound to banking-data-service
     */
    @Bean
    public RestClient bankingDataServiceRestClient(RestClient.Builder restClientBuilder,
                                                    BankingDataServiceProperties properties) {
        return restClientBuilder.baseUrl(properties.baseUrl()).build();
    }
}
