package com.omar.loomdemo.domain;

/**
 * Request payload for {@code POST /notifications/sms}.
 *
 * @param customerId the recipient's customer ID
 * @param message    the SMS message text
 */
public record SmsNotificationRequest(String customerId, String message) {
}
