package com.omar.loomdemo.service;


import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.Account;
import com.omar.loomdemo.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Client for the accounts endpoint on banking-data-service.
 */
@Service
public class AccountService {

    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    private final RestClient restClient;

    public AccountService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches the given customer's current accounts.
     *
     * @param customer the customer to look up accounts for
     * @return the customer's accounts
     */
    public List<Account> getAccountsInfo(Customer customer) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} AccountService.getAccountsInfo(): Start", requestId);

        var accounts = restClient.get()
                .uri("/customer/{id}/accounts", customer.id())
                .retrieve()
                .body(new ParameterizedTypeReference<List<Account>>() { });

        logger.info("{} AccountService.getAccountsInfo(): Done", requestId);
        return accounts;
    }
}
