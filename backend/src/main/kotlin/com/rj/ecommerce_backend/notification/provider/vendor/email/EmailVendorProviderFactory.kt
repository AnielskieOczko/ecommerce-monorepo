package com.rj.ecommerce_backend.notification.provider.vendor.email

import com.rj.ecommerce_backend.notification.config.AppProperties
import org.springframework.stereotype.Component

// This factory should be a Spring component to be injectable
@Component
class EmailVendorProviderFactory(
    // Spring injects all beans that implement EmailVendorProvider
    providerBeans: List<EmailVendorProvider>,
    private val appProperties: AppProperties
) {
    // This creates a map for efficient O(1) lookups, e.g., "smtp" -> SmtpMailVendorProvider
    private val providers: Map<String, EmailVendorProvider> =
        providerBeans.associateBy { it.getProviderType() }

    /**
     * Retrieves the active email vendor provider based on the application.yml configuration.
     *
     * @return The configured and available EmailVendorProvider bean.
     * @throws IllegalStateException if the configuration is missing or no matching provider bean is found.
     */
    fun getActiveVendor(): EmailVendorProvider {
        // Step 1: Safely access the configuration for the "email" channel.
        // If it's missing, throw a clear error.
        val emailChannelConfig = appProperties.notification.channels["email"]
            ?: throw IllegalStateException("Configuration for the 'email' channel is missing in application.yml.")

        // Step 2: Extract the name of the active provider (e.g., "smtp") from the config object.
        val activeProviderName = emailChannelConfig.activeProvider

        // Step 3: Use the extracted name (String) as the key to find the correct provider bean.
        // If no bean with that key exists, throw a clear error.
        return providers[activeProviderName]
            ?: throw IllegalStateException("No email vendor provider bean found for configured type: '$activeProviderName'")
    }
}