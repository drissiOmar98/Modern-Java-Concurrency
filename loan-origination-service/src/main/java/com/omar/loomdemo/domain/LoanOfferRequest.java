package com.omar.loomdemo.domain;

import java.util.List;

/**
 * Request payload sent to banking-data-service to calculate an offer, once
 * the applicant's accounts, loans and credit score have been collected.
 *
 * @param accounts    the applicant's current accounts
 * @param loans       the applicant's existing loans
 * @param creditScore the applicant's credit score
 * @param amount      the requested loan amount
 * @param purpose     the stated purpose of the loan
 */
public record LoanOfferRequest(List<Account> accounts, List<Loan> loans, String creditScore, String amount, String purpose) {
}
