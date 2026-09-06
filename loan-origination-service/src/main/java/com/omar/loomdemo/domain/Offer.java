package com.omar.loomdemo.domain;

/**
 * A calculated loan offer returned to the applicant.
 *
 * @param id        the generated offer identifier
 * @param amount    the offered amount
 * @param purpose   the stated purpose of the loan
 * @param interest  the offered interest rate
 * @param offerText a human-readable summary of the offer
 */
public record Offer(String id, String amount, String purpose, String interest, String offerText) {
}
