package com.omar.loomdemo.domain;

/**
 * Outcome of a single notification send attempt, as reported by
 * notification-service (or recorded locally if the call itself failed).
 *
 * @param channel the channel used (e.g. "email", "sms")
 * @param sent    whether delivery succeeded
 */
public record NotificationResult(String channel, boolean sent) {
}
