package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures the simulated latency and per-channel failure rate used by
 * {@code NotificationSimulator}.
 *
 * <p>Bound from properties under the {@code notification-channel} prefix:
 * <pre>{@code
 * notification-channel:
 *   min-seconds: 1
 *   max-seconds: 3
 *   failure-rate-percent: 25
 * }</pre>
 *
 * @param minSeconds          minimum simulated send time, in seconds
 * @param maxSeconds          maximum simulated send time, in seconds
 * @param failureRatePercent  chance (0-100) that a given send fails
 */
@ConfigurationProperties(prefix = "notification-channel")
public record NotificationChannelProperties(long minSeconds, long maxSeconds, int failureRatePercent) {

    public NotificationChannelProperties {
        if (minSeconds <= 0) {
            minSeconds = 1;
        }
        if (maxSeconds <= 0 || maxSeconds < minSeconds) {
            maxSeconds = Math.max(minSeconds, 3);
        }
        if (failureRatePercent < 0 || failureRatePercent > 100) {
            failureRatePercent = 25;
        }
    }
}
