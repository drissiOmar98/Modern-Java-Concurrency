package com.omar.loomdemo.service;


import com.omar.loomdemo.context.RequestContext;
import com.omar.loomdemo.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

/**
 * Client for the customer lookup endpoint on banking-data-service.
 */
@Service
public class CustomerService {

    private static final Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final RestClient restClient;

    public CustomerService(RestClient restClient) {
        this.restClient = restClient;
    }

    /**
     * Fetches the customer record for the given customer ID.
     *
     * @param customerId the customer's unique identifier
     * @return the customer record
     */
    public Customer getCustomer(String customerId) {
        var requestId = RequestContext.getRequestId();
        logger.info("{} CustomerService.getCustomer(): Start", requestId);

        var customer = restClient.get()
                .uri("/customer/{id}", customerId)
                .retrieve()
                .body(Customer.class);

        logger.info("{} CustomerService.getCustomer(): Done", requestId);
        return customer;
    }
}
