package com.omar.loomdemo;

import com.omar.loomdemo.config.BankingDataServiceProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(BankingDataServiceProperties.class)
public class LoanOriginationServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LoanOriginationServiceApplication.class, args);
	}

}
