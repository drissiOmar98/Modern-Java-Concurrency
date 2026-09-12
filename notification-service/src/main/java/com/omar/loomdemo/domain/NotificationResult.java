package com.omar.loomdemo.domain;

/**
 * Outcome of a single notification send attempt.
 *
 * @param channel the channel used (e.g. "email", "sms")
 * @param sent    whether delivery succeeded
 */
public record NotificationResult(String channel, boolean sent) {
}
