package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Type-safe configuration for connecting to the downstream
 * {@code fraud-check-service}.
 *
 * <p>Bound from properties under the {@code fraud-check-service} prefix.
 *
 * @param baseUrl        the root URL of fraud-check-service
 * @param deadlineMillis how long {@code RiskAssessmentService} will wait for
 *                        both checks to complete before cancelling them and
 *                        failing the application
 */
@ConfigurationProperties(prefix = "fraud-check-service")
@Validated
public record FraudCheckServiceProperties(@NotBlank String baseUrl, long deadlineMillis) {

    public FraudCheckServiceProperties {
        if (deadlineMillis <= 0) {
            deadlineMillis = 3_000;
        }
    }
}
