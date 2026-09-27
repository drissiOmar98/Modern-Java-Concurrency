package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;

/**
 * Type-safe configuration for connecting to the downstream
 * {@code notification-service}.
 *
 * <p>Bound from properties under the {@code notification-service} prefix.
 *
 * @param baseUrl the root URL of notification-service
 */
@ConfigurationProperties(prefix = "notification-service")
@Validated
public record NotificationServiceProperties(@NotBlank String baseUrl) {
}
