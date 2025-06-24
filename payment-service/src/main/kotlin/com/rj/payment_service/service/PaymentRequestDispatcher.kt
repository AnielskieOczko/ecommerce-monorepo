package com.rj.payment_service.service

import com.rj.ecommerce.api.shared.messaging.payment.PaymentRequestDTO
import org.springframework.stereotype.Service


@Service
class PaymentRequestDispatcher(strategies: List<PaymentProviderStrategy>) {
    private val strategyMap = strategies.associateBy { it.getProviderIdentifier() }

    fun dispatch(request: PaymentRequestDTO, correlationId: String?) {
        val strategy = strategyMap[request.providerIdentifier]
            ?: throw IllegalStateException("Payment provider strategy not found for: ${request.providerIdentifier}")

        strategy.initiatePayment(request, correlationId)
    }

}