package com.omar.loomdemo.domain;

/**
 * Result of a fraud screening check, as reported by fraud-check-service.
 *
 * @param passed    whether the applicant cleared the fraud check
 * @param riskScore a simulated risk score (lower is safer)
 */
public record FraudCheckResult(boolean passed, int riskScore) {
}
