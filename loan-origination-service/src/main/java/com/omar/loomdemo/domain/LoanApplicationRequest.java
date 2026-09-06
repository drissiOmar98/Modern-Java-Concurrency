package com.omar.loomdemo.domain;

/**
 * Incoming request payload for {@code POST /loan-applications}.
 *
 * @param customerId the applicant's customer ID
 * @param amount     the requested loan amount
 * @param purpose    the stated purpose of the loan
 */
public record LoanApplicationRequest(String customerId, String amount, String purpose) {
}
