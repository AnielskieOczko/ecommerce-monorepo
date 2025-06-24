package com.rj.payment_service.service

import com.rj.ecommerce.api.shared.messaging.payment.PaymentRequestDTO
import com.stripe.model.checkout.Session

interface PaymentProviderStrategy {

    fun initiatePayment(request: PaymentRequestDTO, correlationId: String?)
    fun handleWebhook(payload: String, signature: String?)
    fun getProviderIdentifier(): String
}