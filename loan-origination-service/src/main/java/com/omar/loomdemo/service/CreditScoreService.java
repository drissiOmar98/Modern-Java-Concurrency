package com.omar.loomdemo.service;


import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.CreditScore;
import com.omar.loomdemo.domain.Customer;
import com.omar.loomdemo.exception.LoanOriginationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;

/**
 * Fetches a customer's credit score.
 *
 * <p>banking-data-service exposes two credit bureau endpoints. This service
 * queries both concurrently via Structured Concurrency and returns whichever
 * responds successfully first, cancelling the other — a common pattern for
 * racing redundant providers of the same data.
 */
@Service
public class CreditScoreService {

    private static final Logger logger = LoggerFactory.getLogger(CreditScoreService.class);

    private final RestClient restClient;

    public CreditScoreService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches the customer's credit score from whichever of the two credit
     * bureau endpoints responds successfully first.
     *
     * @param customer the customer to fetch a credit score for
     * @return the customer's credit score
     * @throws LoanOriginationException if the fetch is interrupted
     */
    public CreditScore getCreditScore(Customer customer) {
        try (var scope = StructuredTaskScope.open(Joiner.<CreditScore>anySuccessfulOrThrow())) {
            scope.fork(() -> getCreditScoreFrom("credit-score1", customer));
            scope.fork(() -> getCreditScoreFrom("credit-score2", customer));

            return scope.join();
        } catch (InterruptedException e) {
            throw new LoanOriginationException(e);
        }
    }

    private CreditScore getCreditScoreFrom(String endpoint, Customer customer) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} CreditScoreService.getCreditScore() with {}: Start", requestId, endpoint);

        var score = restClient.get()
                .uri("/customer/{id}/{endpoint}", customer.id(), endpoint)
                .retrieve()
                .body(CreditScore.class);

        logger.info("{} CreditScoreService.getCreditScore() with {}: Done", requestId, endpoint);
        return score;
    }
}
