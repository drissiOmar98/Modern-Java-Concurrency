package com.omar.loomdemo.domain;

import java.util.List;

/**
 * Request payload for calculating a loan offer.
 *
 * @param accounts    the applicant's current accounts
 * @param loans       the applicant's existing loans
 * @param creditScore the applicant's credit score
 * @param amount      the requested loan amount
 * @param purpose     the stated purpose of the loan
 */
public record LoanOfferRequest(List<Account> accounts, List<Loan> loans, String creditScore, String amount, String purpose) {
}
