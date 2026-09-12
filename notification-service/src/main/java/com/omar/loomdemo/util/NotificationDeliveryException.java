package com.omar.loomdemo.util;

/**
 * Thrown by {@link NotificationSimulator} to represent a simulated delivery
 * failure on a given channel.
 */
public class NotificationDeliveryException extends RuntimeException {

    public NotificationDeliveryException(String channel) {
        super("Simulated delivery failure on channel: " + channel);
    }
}
