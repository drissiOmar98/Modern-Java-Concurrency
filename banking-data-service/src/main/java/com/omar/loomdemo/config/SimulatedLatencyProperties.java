package com.omar.loomdemo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configures the artificial delay that {@code ServiceSimulator} adds to
 * every endpoint, to mimic a real backend's latency.
 *
 * <p>Bound from properties under the {@code simulated-latency} prefix, e.g.:
 * <pre>{@code
 * simulated-latency:
 *   min-seconds: 1
 *   max-seconds: 5
 * }</pre>
 *
 * <p>Previously this range was hard-coded in {@code ServicesUtil}; making it
 * a configuration property lets each environment (fast local runs vs. a
 * demo that wants visibly slow calls) tune it without a code change.
 *
 * @param minSeconds the minimum simulated delay, in seconds
 * @param maxSeconds the maximum simulated delay, in seconds
 */
@ConfigurationProperties(prefix = "simulated-latency")
public record SimulatedLatencyProperties(long minSeconds, long maxSeconds) {

    /**
     * Applies sensible defaults (1-5 seconds) when a value is not
     * configured, so the service still runs out of the box.
     */
    public SimulatedLatencyProperties {
        if (minSeconds <= 0) {
            minSeconds = 1;
        }
        if (maxSeconds <= 0 || maxSeconds < minSeconds) {
            maxSeconds = Math.max(minSeconds, 5);
        }
    }
}
