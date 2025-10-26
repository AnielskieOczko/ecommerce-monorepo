package com.rj.notification_service.provider

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import org.springframework.stereotype.Component

@Component
class ChannelProviderFactory(
    // Spring automatically injects a list of all beans that implement ChannelProvider
    providerBeans: List<ChannelProvider>
) {
    // Create a map for efficient O(1) lookups, using the channel type as the key.
    private val providers: Map<NotificationChannel, ChannelProvider> =
        providerBeans.associateBy { it.getChannelType() }

    /**
     * Retrieves the appropriate provider for a given notification channel.
     *
     * @param channel The channel for which to find a provider.
     * @return The corresponding ChannelProvider implementation.
     * @throws UnsupportedOperationException if no provider is registered for the given channel.
     */
    fun getProvider(channel: NotificationChannel): ChannelProvider {
        return providers[channel]
            ?: throw UnsupportedOperationException("No provider configured for channel: $channel")
    }
}