package com.rj.ecommerce_backend.payment.provider

import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.payment.model.PaymentSessionDetails
    interface PaymentProvider {

        /**
         * Takes an internal Order object, builds the provider-specific request,
         * executes the API call, and returns the session details.
         */
        fun initiatePayment(order: Order, successUrl: String, cancelUrl: String): PaymentSessionDetails

        /**
         * Handles asynchronous webhook events from the payment provider.
         */
        fun handleWebhook(payload: String, signature: String?)

        /**
         * Returns a unique identifier for this provider (e.g., "STRIPE").
         */
        fun getProviderIdentifier(): String
    }
