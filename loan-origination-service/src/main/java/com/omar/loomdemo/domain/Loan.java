package com.omar.loomdemo.domain;

/**
 * An existing loan held by a customer.
 *
 * @param number the loan number
 * @param amount the outstanding amount, as a decimal string
 */
public record Loan(String number, String amount) {
}
