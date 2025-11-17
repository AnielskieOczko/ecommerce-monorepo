package com.rj.ecommerce_backend.payment.config

import com.stripe.Stripe
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
// It now depends on the main PaymentProperties class
class StripeConfig(private val paymentProperties: PaymentProperties) {

    @PostConstruct
    fun initStripe() {
        // Find the 'stripe' configuration within the providers map
        val stripeConfig = paymentProperties.providers["stripe"]

        // Only initialize Stripe if the provider is enabled and the key is present
        if (stripeConfig?.enabled == true && !stripeConfig.apiKey.isNullOrBlank()) {
            Stripe.apiKey = stripeConfig.apiKey
        }
    }
}