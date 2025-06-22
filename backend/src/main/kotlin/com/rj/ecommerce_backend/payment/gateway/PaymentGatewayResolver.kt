package com.rj.ecommerce_backend.payment.gateway

import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.payment.config.PaymentProperties
import org.springframework.stereotype.Service

@Service
class PaymentGatewayResolver(
    private val paymentProperties: PaymentProperties,
    // Spring injects all beans that implement the PaymentGateway interface
    private val availableGateways: List<PaymentGateway>
) {
    // This map holds the ready-to-use gateway instances, keyed by their identifier string.
    private val gatewayByIdentifierMap: Map<String, PaymentGateway> =
        availableGateways.associateBy { it.getGatewayIdentifier() }

    /**
     * Finds and returns the appropriate PaymentGateway for a given PaymentMethod.
     * The mapping is defined in application.yml.
     */
    fun resolve(method: PaymentMethod): PaymentGateway {
        // 1. Look up the gateway identifier string (e.g., "STRIPE_GATEWAY") from the properties.
        val gatewayIdentifier = paymentProperties.methodToGatewayMapping[method]
            ?: throw IllegalStateException("No payment gateway is configured for payment method: $method")

        // 2. Use the identifier to get the actual gateway bean instance from our map.
        return gatewayByIdentifierMap[gatewayIdentifier]
            ?: throw IllegalStateException("Configuration error: The configured gateway '$gatewayIdentifier' does not exist.")
    }
}