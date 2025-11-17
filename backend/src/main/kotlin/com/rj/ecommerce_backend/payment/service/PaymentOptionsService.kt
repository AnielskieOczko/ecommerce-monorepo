package com.rj.ecommerce_backend.payment.service

import com.rj.ecommerce_backend.api.shared.dto.payment.response.PaymentOptionDetails
import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.payment.config.PaymentProperties
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PaymentOptionsService(
    private val paymentProperties: PaymentProperties
) {
    @Cacheable("payment-options")
    fun getAvailablePaymentOptions(): List<PaymentOptionDetails> {
        return paymentProperties.providers
            .filter { (_, config) -> config.enabled }
            .flatMap { (_, config) ->
                // For each method, create the new, simpler DTO
                config.supportedMethods.map { method ->
                    mapToPaymentOptionDetails(method, config.displayName)
                }
            }
            .distinctBy { it.method }
    }

    // This method now builds the simpler DTO
    private fun mapToPaymentOptionDetails(method: PaymentMethod, displayName: String): PaymentOptionDetails {
        return PaymentOptionDetails(
            displayName = displayName,
            method = method,
            iconUrl = getMethodIcon(method)
        )
    }

    private fun getMethodIcon(method: PaymentMethod): String = when (method) {
        PaymentMethod.CREDIT_CARD -> "/icons/credit_card.svg"
        PaymentMethod.BLIK -> "/icons/blik.svg"
        PaymentMethod.BANK_TRANSFER -> "/icons/bank_transfer.svg"
    }
}