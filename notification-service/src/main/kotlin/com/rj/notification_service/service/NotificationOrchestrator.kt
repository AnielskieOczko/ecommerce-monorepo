package com.rj.notification_service.service

import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationRequest
import com.rj.notification_service.provider.ChannelProviderFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NotificationOrchestrator(
    private val channelProviderFactory: ChannelProviderFactory
) {
    fun process(request: NotificationRequest<Any>) {
        val envelope = request.envelope
        logger.info { "Processing multi-channel request for correlationId: ${envelope.correlationId}, channels: ${envelope.channels}" }

        // Iterate over the set of channels from the envelope.
        envelope.channels.forEach { channel ->
            try {
                // Get the specific provider for the current channel in the loop.
                val channelProvider = channelProviderFactory.getProvider(channel)

                // Delegate the entire request to that provider.
                // The provider will know how to handle the payload for its specific channel.
                channelProvider.process(request)

                logger.info { "Successfully delegated request to ${channel.name} provider for correlationId: ${envelope.correlationId}" }
            } catch (e: Exception) {
                // Log the error for the failed channel but continue the loop to try others.
                // This makes the system resilient; a failure in SMS shouldn't stop the email from sending.
                logger.error(e) { "Failed to process channel ${channel.name} for correlationId: ${envelope.correlationId}. Other channels may still succeed." }
            }
        }
    }
}