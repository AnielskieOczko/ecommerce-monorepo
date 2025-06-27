package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import org.springframework.stereotype.Component

@Component
class NotificationProviderFactory(
    val providerBeans: List<NotificationProvider>
) {
    private val providers = providerBeans.associateBy { provider -> provider.getChannel() }

    fun getProvider(channel: NotificationChannel): NotificationProvider {
        return providers[channel]
            ?: throw UnsupportedOperationException("No provider configured for channel: $channel")
    }
}