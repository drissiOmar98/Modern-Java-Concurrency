package com.omar.loomdemo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BankingDataServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(BankingDataServiceApplication.class, args);
	}

}
