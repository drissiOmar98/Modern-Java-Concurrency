package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures the simulated latency and failure behaviour of this service's
 * checks.
 *
 * <p>Bound from properties under the {@code risk-check} prefix:
 * <pre>{@code
 * risk-check:
 *   min-seconds: 1
 *   max-seconds: 4
 *   slow-check-chance-percent: 20
 * }</pre>
 *
 * <p>{@code slowCheckChancePercent} controls how often a check deliberately
 * overruns {@code maxSeconds} (up to 3x as long), so callers using a
 * deadline-bound join — see {@code RiskAssessmentService} in
 * loan-origination-service — actually observe a timeout now and then instead
 * of the demo always looking fast and reliable.
 *
 * @param minSeconds              minimum simulated processing time, in seconds
 * @param maxSeconds              maximum simulated processing time, in seconds
 * @param slowCheckChancePercent  chance (0-100) that a check runs far slower than normal
 */
@ConfigurationProperties(prefix = "risk-check")
public record RiskCheckProperties(long minSeconds, long maxSeconds, int slowCheckChancePercent) {

    public RiskCheckProperties {
        if (minSeconds <= 0) {
            minSeconds = 1;
        }
        if (maxSeconds <= 0 || maxSeconds < minSeconds) {
            maxSeconds = Math.max(minSeconds, 4);
        }
        if (slowCheckChancePercent < 0 || slowCheckChancePercent > 100) {
            slowCheckChancePercent = 20;
        }
    }
}
