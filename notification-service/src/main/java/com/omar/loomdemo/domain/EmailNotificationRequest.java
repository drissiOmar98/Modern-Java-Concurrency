package com.omar.loomdemo.domain;

/**
 * Request payload for {@code POST /notifications/email}.
 *
 * @param customerId the recipient's customer ID
 * @param subject    the email subject
 * @param body       the email body
 */
public record EmailNotificationRequest(String customerId, String subject, String body) {
}
