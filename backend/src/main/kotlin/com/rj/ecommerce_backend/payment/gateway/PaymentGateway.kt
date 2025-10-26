package com.rj.ecommerce_backend.payment.gateway

import com.rj.ecommerce.api.shared.messaging.payment.request.PaymentInitiationRequest
import com.rj.ecommerce_backend.order.domain.Order

interface PaymentGateway {
    fun buildPaymentRequest(order: Order, successUrl: String, cancelUrl: String): PaymentInitiationRequest

    // Returns a unique, stable key for this gateway (e.g., "STRIPE_GATEWAY")
    fun getGatewayIdentifier(): String
}