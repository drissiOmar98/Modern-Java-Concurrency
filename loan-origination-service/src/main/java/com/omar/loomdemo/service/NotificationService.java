package com.omar.loomdemo.service;

import com.omar.loomdemo.context.NotificationContext;
import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.Customer;
import com.omar.loomdemo.domain.NotificationResult;
import com.omar.loomdemo.domain.Offer;
import com.omar.loomdemo.exception.LoanOriginationException;
import com.omar.loomdemo.joiner.BestEffortJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.StructuredTaskScope;

/**
 * Dispatches the decision on a loan application to the applicant over
 * multiple channels concurrently.
 *
 * <p>This is the one part of the loan flow where a channel failing is
 * <em>not</em> fatal to the request — the offer has already been
 * calculated by the time this runs. That's exactly the scenario
 * {@link BestEffortJoiner} exists for: every channel is tried, and whichever
 * succeed are reported back, instead of the whole dispatch failing because
 * one channel had a bad day.
 */
@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final RestClient restClient;

    public NotificationService(@Qualifier("notificationServiceRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Notifies the applicant of their offer over email and SMS concurrently,
     * tolerating individual channel failures.
     *
     * <p>Runs inside {@link NotificationContext#run}, which nests a
     * best-effort flag inside the already-bound request-ID scope from
     * {@link RequestContext} — both are visible to the forked subtasks
     * below without any extra plumbing.
     *
     * @param customer the applicant
     * @param offer    the calculated offer being communicated
     * @return the notifications that were delivered successfully
     */
    public List<NotificationResult> notifyOfferDecision(Customer customer, Offer offer) {
        var requestId = RequestContext.getRequestId();
        var delivered = new java.util.concurrent.atomic.AtomicReference<List<NotificationResult>>(List.of());

        NotificationContext.run(() -> {
            try (var scope = StructuredTaskScope.open(new BestEffortJoiner<NotificationResult>())) {
                scope.fork(() -> sendEmail(customer, offer));
                scope.fork(() -> sendSms(customer, offer));

                delivered.set(scope.join());
            } catch (InterruptedException e) {
                throw new LoanOriginationException(e);
            }
        });

        logger.info("{} NotificationService: delivered on {} of 2 channels", requestId, delivered.get().size());
        return delivered.get();
    }

    private NotificationResult sendEmail(Customer customer, Offer offer) {
        return sendVia("email", () -> restClient.post()
                .uri("/notifications/email")
                .body(Map.of(
                        "customerId", customer.id(),
                        "subject", "Your loan offer is ready",
                        "body", offer.offerText()))
                .retrieve()
                .body(NotificationResult.class));
    }

    private NotificationResult sendSms(Customer customer, Offer offer) {
        return sendVia("sms", () -> restClient.post()
                .uri("/notifications/sms")
                .body(Map.of(
                        "customerId", customer.id(),
                        "message", "Your loan offer #" + offer.id() + " is ready"))
                .retrieve()
                .body(NotificationResult.class));
    }

    /**
     * Runs a single channel send, converting any failure into a
     * {@code sent = false} result rather than propagating the exception —
     * consistent with {@link NotificationContext#isBestEffort()} being
     * {@code true} for every subtask forked from {@link #notifyOfferDecision}.
     */
    private NotificationResult sendVia(String channel, java.util.function.Supplier<NotificationResult> call) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} NotificationService.sendVia({}): Start", requestId, channel);
        try {
            var result = call.get();
            logger.info("{} NotificationService.sendVia({}): Done", requestId, channel);
            return result;
        } catch (RuntimeException e) {
            if (!NotificationContext.isBestEffort()) {
                throw e;
            }
            logger.warn("{} NotificationService.sendVia({}): failed, continuing (best-effort)", requestId, channel);
            return new NotificationResult(channel, false);
        }
    }
}
