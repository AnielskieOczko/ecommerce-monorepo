package com.rj.ecommerce_backend.payment.provider

import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.payment.config.PaymentProperties
import org.springframework.stereotype.Service

@Service
class PaymentProviderResolver(
    private val paymentProperties: PaymentProperties,
    // <-- CHANGED: Spring now injects all beans that implement the NEW interface
    private val availableProviders: List<PaymentProvider>
) {
    // <-- CHANGED: The map now holds PaymentProvider instances
    private val providerByIdentifierMap: Map<String, PaymentProvider> =
        availableProviders.associateBy { it.getProviderIdentifier() }

    /**
     * Finds and returns the appropriate PaymentProvider for a given PaymentMethod.
     */
    fun resolve(method: PaymentMethod): PaymentProvider {
        val providerIdentifier = paymentProperties.methodToGatewayMapping[method]
            ?: throw IllegalStateException("No payment provider is configured for payment method: $method")

        return providerByIdentifierMap[providerIdentifier]
            ?: throw IllegalStateException("Configuration error: The configured provider '$providerIdentifier' does not exist.")
    }
}