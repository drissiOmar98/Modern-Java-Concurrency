package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Type-safe configuration for connecting to the downstream
 * {@code banking-data-service}.
 *
 * <p>Bound from properties under the {@code banking-data-service} prefix,
 * e.g. in {@code application.yml}:
 * <pre>{@code
 * banking-data-service:
 *   base-url: http://localhost:8081
 * }</pre>
 *
 * <p>Using a dedicated properties class (instead of scattering
 * {@code @Value} lookups across services) keeps all downstream connection
 * settings in one validated, discoverable place.
 *
 * @param baseUrl the root URL of banking-data-service
 */
@ConfigurationProperties(prefix = "banking-data-service")
@Validated
public record BankingDataServiceProperties(@NotBlank String baseUrl) {
}
