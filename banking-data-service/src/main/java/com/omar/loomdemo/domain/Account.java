package com.omar.loomdemo.domain;

/**
 * An account held by a customer.
 *
 * @param number  the account number
 * @param balance the current balance, as a decimal string
 */
public record Account(String number, String balance) {
}
