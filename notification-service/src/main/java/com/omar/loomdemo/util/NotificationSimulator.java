package com.omar.loomdemo.util;


import com.omar.loomdemo.config.NotificationChannelProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Simulates sending a notification through a channel (email, SMS, ...),
 * including a configurable chance of outright failure — standing in for a
 * flaky third-party provider.
 */
@Component
public class NotificationSimulator {

    private static final Logger logger = LoggerFactory.getLogger(NotificationSimulator.class);

    private final NotificationChannelProperties properties;

    public NotificationSimulator(NotificationChannelProperties properties) {
        this.properties = properties;
    }

    /**
     * Blocks the current thread to simulate the given channel taking real
     * time to send, then randomly fails according to the configured failure
     * rate.
     *
     * @param channel a short, human-readable channel name (e.g. "email")
     * @throws NotificationDeliveryException if the simulated send fails
     */
    public void send(String channel) {
        long min = properties.minSeconds();
        long max = properties.maxSeconds();
        long delaySeconds = min + (long) (Math.random() * (max - min + 1));

        logger.info("Sending via {}. Time to complete: {} seconds", channel, delaySeconds);
        try {
            Thread.sleep(delaySeconds * 1_000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }

        boolean fail = Math.random() * 100 < properties.failureRatePercent();
        if (fail) {
            logger.warn("Delivery via {} failed (simulated)", channel);
            throw new NotificationDeliveryException(channel);
        }
        logger.info("Delivered via {}", channel);
    }
}
