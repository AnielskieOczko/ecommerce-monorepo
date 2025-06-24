package com.rj.payment_service.config

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
class StripeConfig(private val stripeProperties: StripeProperties) {

    @PostConstruct
    fun initStripe() {
        Stripe.apiKey = stripeProperties.secretKey
    }
}