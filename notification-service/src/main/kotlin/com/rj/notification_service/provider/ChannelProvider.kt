package com.rj.notification_service.provider

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationRequest

/**
 * Defines the contract for a component that can process a notification for a specific channel (e.g., EMAIL, SMS).
 */
interface ChannelProvider {
    /**
     * The specific channel this provider handles.
     */
    fun getChannelType(): NotificationChannel

    /**
     * Processes the generic notification request for this channel.
     * This method is responsible for mapping the generic payload to a channel-specific model
     * and delegating to the appropriate vendor provider.
     */
    fun process(request: NotificationRequest<Any>)
}