package com.rj.ecommerce_backend.payment.provider

import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.payment.config.PaymentProperties
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class PaymentProviderResolver(
    private val paymentProperties: PaymentProperties,
    // Spring injects all beans that implement the PaymentProvider interface (e.g., StripePaymentProvider)
    private val availableProviderBeans: List<PaymentProvider>
) {
    /**
     * A pre-computed map for fast, O(1) lookup of the correct provider for a given payment method.
     * This map is built once at application startup.
     */
    private lateinit var methodToProviderMap: Map<PaymentMethod, PaymentProvider>

    /**
     * This method runs once after the bean has been constructed. It validates the payment
     * provider configuration from application.yml and builds the method-to-provider mapping.
     *
     * It will throw an IllegalStateException on startup if:
     * 1. A provider is enabled in YAML but no matching Spring bean is found.
     * 2. Two or more active providers claim to support the same PaymentMethod.
     */
    @PostConstruct
    fun initialize() {
        // Create a temporary map of available provider beans by their identifier (e.g., "STRIPE" -> StripePaymentProvider instance)
        val beansByIdentifier = availableProviderBeans.associateBy { it.getProviderIdentifier() }
        val finalMap = mutableMapOf<PaymentMethod, PaymentProvider>()

        // 1. Filter down to only the providers that are enabled in the configuration
        val enabledProviders = paymentProperties.providers
            .filter { (_, providerConfig) -> providerConfig.enabled }

        // 2. Iterate over each enabled provider from the configuration
        enabledProviders.forEach { (providerKey, providerConfig) ->
            // 3. Find the corresponding Spring @Component that implements this provider
            val providerBean = beansByIdentifier[providerKey.uppercase()]
                ?: throw IllegalStateException(
                    "Configuration error: Provider '$providerKey' is enabled in application.yml but no matching PaymentProvider bean was found. " +
                            "Ensure a bean exists that returns '${providerKey.uppercase()}' from getProviderIdentifier()."
                )

            // 4. For each payment method this provider supports, add it to our final map
            providerConfig.supportedMethods.forEach { method ->
                // 5. CRITICAL VALIDATION: Ensure no other active provider has already claimed this method.
                if (finalMap.containsKey(method)) {
                    throw IllegalStateException(
                        "Configuration error: The PaymentMethod '$method' is claimed by multiple active providers. " +
                                "This is an ambiguous configuration. Please assign each method to only one active provider."
                    )
                }
                // 6. If the check passes, create the mapping.
                finalMap[method] = providerBean
            }
        }

        // 7. Assign the constructed map to the class property.
        methodToProviderMap = finalMap.toMap()
    }

    /**
     * Finds and returns the appropriate PaymentProvider for a given PaymentMethod using the pre-computed map.
     */
    fun resolve(method: PaymentMethod): PaymentProvider {
        // The logic is now a simple, ultra-fast map lookup.
        return methodToProviderMap[method]
            ?: throw IllegalStateException("No active and configured payment provider was found for the payment method: '$method'")
    }
}