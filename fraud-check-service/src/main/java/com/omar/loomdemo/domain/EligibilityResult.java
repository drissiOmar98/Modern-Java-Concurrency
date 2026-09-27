package com.omar.loomdemo.domain;

/**
 * Result of an eligibility screening check.
 *
 * @param eligible whether the applicant is eligible for the requested loan
 * @param reason   a short human-readable reason for the decision
 */
public record EligibilityResult(boolean eligible, String reason) {
}
