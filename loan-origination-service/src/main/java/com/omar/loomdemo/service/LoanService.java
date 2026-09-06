package com.omar.loomdemo.service;


import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client for the loans and offer-calculation endpoints on
 * banking-data-service.
 */
@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final RestClient restClient;

    public LoanService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches the given customer's existing loans.
     *
     * @param customer the customer to look up loans for
     * @return the customer's existing loans
     */
    public List<Loan> getLoansInfo(Customer customer) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} LoanService.getLoansInfo(): Start", requestId);

        var loans = restClient.get()
                .uri("/customer/{id}/loans", customer.id())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Loan>>() { });

        logger.info("{} LoanService.getLoansInfo(): Done", requestId);
        return loans;
    }

    /**
     * Calculates a loan offer for the customer based on their existing
     * financial position and the requested loan terms.
     *
     * @param customer     the applicant
     * @param accountsInfo the applicant's current accounts
     * @param loansInfo    the applicant's existing loans
     * @param creditScore  the applicant's credit score
     * @param amount       the requested loan amount
     * @param purpose      the stated purpose of the loan
     * @return the calculated offer
     */
    public Offer calculateOffer(Customer customer,
                                List<Account> accountsInfo,
                                List<Loan> loansInfo,
                                CreditScore creditScore,
                                String amount,
                                String purpose) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} LoanService.calculateOffer(): Start", requestId);

        var loanOfferRequest = new LoanOfferRequest(accountsInfo, loansInfo, creditScore.score(), amount, purpose);
        var offer = restClient.post()
                .uri("/customer/{id}/loans/offer", customer.id())
                .body(loanOfferRequest)
                .retrieve()
                .body(Offer.class);

        logger.info("{} LoanService.calculateOffer(): Done", requestId);
        return offer;
    }
}
