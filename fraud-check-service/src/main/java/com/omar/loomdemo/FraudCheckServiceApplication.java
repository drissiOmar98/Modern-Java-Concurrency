package com.omar.loomdemo;

import com.omar.loomdemo.config.RiskCheckProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Entry point for the Fraud Check Service.
 *
 * <p>Simulates a backend that performs fraud detection and eligibility
 * screening for a loan applicant. It exists specifically to give
 * {@code loan-origination-service} a second, independent downstream
 * dependency — one whose two checks are called concurrently under a
 * {@code StructuredTaskScope} that is joined with a hard deadline, so a slow
 * or hung check can't stall the whole application indefinitely.
 */
@SpringBootApplication
@EnableConfigurationProperties(RiskCheckProperties.class)
public class FraudCheckServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudCheckServiceApplication.class, args);
	}

}
