package com.rj.ecommerce_email_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class EcommerceEmailServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceEmailServiceApplication.class, args);
	}

}
