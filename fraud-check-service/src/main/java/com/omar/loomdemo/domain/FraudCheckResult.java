package com.omar.loomdemo.domain;

/**
 * Result of a fraud screening check.
 *
 * @param passed   whether the applicant cleared the fraud check
 * @param riskScore a simulated risk score (lower is safer)
 */
public record FraudCheckResult(boolean passed, int riskScore) {
}
