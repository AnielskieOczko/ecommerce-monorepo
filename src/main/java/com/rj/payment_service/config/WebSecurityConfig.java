package com.rj.payment_service.config;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "stripe")
@Getter
@Setter
@Validated
@Slf4j
public class WebSecurityConfig {

    private String secretKey;
    private String publishableKey;
    private String webhookSecret;

    public WebSecurityConfig() {
        log.info("StripeProperties bean created.");
    }

    public void setSecretKey(String secretKey,
                             String publishableKey,
                             String webhookSecret) {
        this.secretKey = secretKey;
        this.publishableKey = publishableKey;
        this.webhookSecret = webhookSecret;
        log.info("Stripe secret key set in StripeProperties: {}", secretKey != null ? "***SET***" : "null");
    }

}
