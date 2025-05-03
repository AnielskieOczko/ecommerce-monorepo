package com.rj.payment_service.config;

import com.stripe.Stripe;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
@Slf4j
@EnableConfigurationProperties(WebSecurityConfig.class)
public class StripeConfig {

    private final WebSecurityConfig stripeProperties;

    StripeConfig(WebSecurityConfig stripeProperties) {
        this.stripeProperties = stripeProperties;
        log.info("StripeConfig constructor called.  Secret Key from properties: {}", stripeProperties.getSecretKey() != null ? "***SET***" : "null");
        Stripe.apiKey = stripeProperties.getSecretKey();
        log.info("Stripe secret key set in StripeConfig: {}", stripeProperties.getSecretKey() != null ? "***SET***" : "null");
    }

    @Bean
    public RestTemplate stripeRestTemplate() {
        // Configure a dedicated RestTemplate for Stripe API calls
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(
                new SimpleClientHttpRequestFactory()));
        // Add interceptors for logging, metrics, etc.
        return restTemplate;
    }


}
