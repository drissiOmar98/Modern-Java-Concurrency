package com.omar.loomdemo.controller;


import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.*;
import com.omar.loomdemo.exception.LoanOriginationException;
import com.omar.loomdemo.service.AccountService;
import com.omar.loomdemo.service.CreditScoreService;
import com.omar.loomdemo.service.CustomerService;
import com.omar.loomdemo.service.LoanService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;

/**
 * Handles incoming loan applications.
 *
 * <p>For each application, a request-scoped correlation ID is bound via
 * {@link RequestContext}, then the applicant's accounts, existing loans and
 * credit score are fetched concurrently using Structured Concurrency before
 * an offer is calculated.
 */
@RestController
public class LoanApplicationController {

    private final CustomerService customerService;
    private final AccountService accountService;
    private final LoanService loanService;
    private final CreditScoreService creditScoreService;

    public LoanApplicationController(CustomerService customerService,
                                      AccountService accountService,
                                      LoanService loanService,
                                      CreditScoreService creditScoreService) {
        this.customerService = customerService;
        this.accountService = accountService;
        this.loanService = loanService;
        this.creditScoreService = creditScoreService;
    }

    /**
     * Accepts a loan application and returns a calculated offer.
     *
     * @param request the loan application details
     * @return the calculated offer
     */
    @PostMapping("/loan-applications")
    public Offer applyForLoan(@RequestBody LoanApplicationRequest request) {
        var requestId = UUID.randomUUID();

        return RequestContext.withRequestId(requestId).call(() -> {
            var currentCustomer = customerService.getCustomer(request.customerId());
            var customerInfo = fetchCustomerInfo(currentCustomer);

            return loanService.calculateOffer(
                    currentCustomer,
                    customerInfo.accounts(),
                    customerInfo.loans(),
                    customerInfo.creditScore(),
                    request.amount(),
                    request.purpose());
        });
    }

    @PostMapping("/loan-application")
    public Offer applyForLoan(@RequestBody LoanApplicationRequest request) {
        var requestID = UUID.randomUUID();

        return RequestContext.withRequestId(requestID)
                .call(() -> {
                    var currentCustomer = customerService.getCustomer(request.customerId());

                    var customerInfo = getCustomerInfo(currentCustomer);
                    var offer = loanService.calculateOffer(
                            currentCustomer, customerInfo.accounts(), customerInfo.loans(), customerInfo.creditScore(), request.amount(), request.purpose()
                    );
                    return offer;
                });
    }


    /**
     * Bundles the applicant's accounts, loans and credit score, fetched
     * concurrently.
     */
    private record CustomerInfo(List<Account> accounts, List<Loan> loans, CreditScore creditScore) {
    }

    /**
     * Fetches the applicant's accounts, existing loans and credit score
     * concurrently, using a {@link StructuredTaskScope} so that if any one
     * fetch fails or the request is cancelled, the others are cancelled too.
     *
     * @param customer the applicant
     * @return the applicant's bundled account, loan and credit-score data
     * @throws LoanOriginationException if the fetch is interrupted
     */
    private CustomerInfo fetchCustomerInfo(Customer customer) {
        try (var scope = StructuredTaskScope.open()) {
            var accountsTask = scope.fork(() -> accountService.getAccountsInfo(customer));
            var loansTask = scope.fork(() -> loanService.getLoansInfo(customer));
            var creditScoreTask = scope.fork(() -> creditScoreService.getCreditScore(customer));

            scope.join();

            return new CustomerInfo(accountsTask.get(), loansTask.get(), creditScoreTask.get());
        } catch (InterruptedException e) {
            throw new LoanOriginationException(e);
        }
    }
}
