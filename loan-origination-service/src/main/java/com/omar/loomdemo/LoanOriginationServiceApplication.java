package com.omar.loomdemo;

import com.omar.loomdemo.config.BankingDataServiceProperties;
import com.omar.loomdemo.config.FraudCheckServiceProperties;
import com.omar.loomdemo.config.NotificationServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({BankingDataServiceProperties.class, FraudCheckServiceProperties.class, NotificationServiceProperties.class})
public class LoanOriginationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanOriginationServiceApplication.class, args);
	}

}
