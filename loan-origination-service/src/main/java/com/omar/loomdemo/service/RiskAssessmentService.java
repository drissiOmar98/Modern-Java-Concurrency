package com.omar.loomdemo.service;


import com.omar.loomdemo.config.FraudCheckServiceProperties;
import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.Customer;
import com.omar.loomdemo.domain.EligibilityResult;
import com.omar.loomdemo.domain.FraudCheckResult;
import com.omar.loomdemo.domain.RiskAssessment;
import com.omar.loomdemo.exception.LoanOriginationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Instant;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Joiner;
import java.util.concurrent.TimeoutException;

/**
 * Runs fraud and eligibility screening for an applicant concurrently,
 * bounded by a hard deadline.
 *
 * <p>Unlike the fan-out in {@code LoanApplicationController} — which waits
 * as long as it takes — this join is time-bounded via
 * {@link StructuredTaskScope#joinUntil(Instant)}. If fraud-check-service is
 * slow or hung, both subtasks are cancelled once the deadline passes and the
 * application fails fast, rather than leaving the caller waiting
 * indefinitely on a downstream dependency that may never respond.
 */
@Service
public class RiskAssessmentService {

    private static final Logger logger = LoggerFactory.getLogger(RiskAssessmentService.class);

    private final RestClient restClient;
    private final long deadlineMillis;

    public RiskAssessmentService(@Qualifier("fraudCheckServiceRestClient") RestClient restClient,
                                  FraudCheckServiceProperties properties) {
        this.restClient = restClient;
        this.deadlineMillis = properties.deadlineMillis();
    }

    /**
     * Runs the fraud and eligibility checks concurrently and waits for both,
     * up to the configured deadline.
     *
     * @param customer the applicant
     * @param amount   the requested loan amount
     * @return the combined risk assessment
     * @throws LoanOriginationException if the deadline is exceeded or the
     *                                    fetch is interrupted
     */
    public RiskAssessment assess(Customer customer, String amount) {
        var requestId = RequestContext.getRequestId();

        try (var scope = StructuredTaskScope.open(Joiner.<Object>awaitAllSuccessfulOrThrow())) {
            var fraudCheckTask = scope.fork(() -> fetchFraudCheck(customer, amount));
            var eligibilityTask = scope.fork(() -> fetchEligibility(customer, amount));

            var deadline = Instant.now().plusMillis(deadlineMillis);
            scope.joinUntil(deadline);

            return new RiskAssessment(fraudCheckTask.get(), eligibilityTask.get());
        } catch (TimeoutException e) {
            logger.warn("{} RiskAssessmentService.assess(): Deadline of {}ms exceeded, cancelling checks",
                    requestId, deadlineMillis);
            throw new LoanOriginationException("Risk assessment timed out after " + deadlineMillis + "ms");
        } catch (InterruptedException e) {
            throw new LoanOriginationException(e);
        }
    }

    private FraudCheckResult fetchFraudCheck(Customer customer, String amount) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} RiskAssessmentService.fetchFraudCheck(): Start", requestId);

        var result = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}/fraud-check")
                        .queryParam("amount", amount)
                        .build(customer.id()))
                .retrieve()
                .body(FraudCheckResult.class);

        logger.info("{} RiskAssessmentService.fetchFraudCheck(): Done", requestId);
        return result;
    }

    private EligibilityResult fetchEligibility(Customer customer, String amount) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} RiskAssessmentService.fetchEligibility(): Start", requestId);

        var result = restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/customers/{id}/eligibility-check")
                        .queryParam("amount", amount)
                        .build(customer.id()))
                .retrieve()
                .body(EligibilityResult.class);

        logger.info("{} RiskAssessmentService.fetchEligibility(): Done", requestId);
        return result;
    }
}
