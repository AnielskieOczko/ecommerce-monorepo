package com.rj.payment_service.service

import com.rj.ecommerce.api.shared.messaging.payment.request.PaymentInitiationRequest

interface PaymentProviderStrategy {

    fun initiatePayment(request: PaymentInitiationRequest, correlationId: String?)
    fun handleWebhook(payload: String, signature: String?)
    fun getProviderIdentifier(): String
}