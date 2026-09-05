package com.omar.loomdemo.controller;


import com.omar.loomdemo.domain.*;
import com.omar.loomdemo.util.ServiceSimulator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes dummy customer, account, loan, credit-score and offer-calculation
 * endpoints, each with a simulated processing delay applied via
 * {@link ServiceSimulator}.
 */
@RestController
@RequestMapping("/customer")
public class CustomerController {

    private final ServiceSimulator serviceSimulator;

    public CustomerController(ServiceSimulator serviceSimulator) {
        this.serviceSimulator = serviceSimulator;
    }

    /**
     * Returns a dummy customer record for the given ID.
     */
    @GetMapping("/{id}")
    public Customer getCustomer(@PathVariable("id") String customerId) {
        serviceSimulator.simulate("getCustomer");
        return new Customer(customerId);
    }

    /**
     * Returns a dummy credit score, standing in for the first of two credit
     * bureaus.
     */
    @GetMapping("/{id}/credit-score1")
    public CreditScore getCreditScore1(@PathVariable("id") String customerId) {
        serviceSimulator.simulate("getCreditScore1");
        return new CreditScore("Score1");
    }

    /**
     * Returns a dummy credit score, standing in for the second of two
     * credit bureaus.
     */
    @GetMapping("/{id}/credit-score2")
    public CreditScore getCreditScore2(@PathVariable("id") String customerId) {
        serviceSimulator.simulate("getCreditScore2");
        return new CreditScore("Score2");
    }

    /**
     * Returns dummy account data for the given customer.
     */
    @GetMapping("/{id}/accounts")
    public List<Account> getAccountsInfo(@PathVariable("id") String customerId) {
        serviceSimulator.simulate("getAccountsInfo");
        return List.of(new Account("123", "1000.00"), new Account("456", "2000.00"));
    }

    /**
     * Returns dummy existing-loan data for the given customer.
     */
    @GetMapping("/{id}/loans")
    public List<Loan> getLoansInfo(@PathVariable("id") String customerId) {
        serviceSimulator.simulate("getLoansInfo");
        return List.of(new Loan("TL123", "10000.00"), new Loan("CL456", "20000.00"));
    }

    /**
     * Calculates and returns a dummy loan offer for the given customer.
     */
    @PostMapping("/{id}/loans/offer")
    public Offer calculateOffer(@PathVariable("id") String customerId, @RequestBody LoanOfferRequest request) {
        serviceSimulator.simulate("calculateOffer");
        return new Offer("LMN123", request.amount(), request.purpose(), "4.00", "An offer for your loan application");
    }
}
