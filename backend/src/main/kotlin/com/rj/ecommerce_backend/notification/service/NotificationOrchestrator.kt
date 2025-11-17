package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.provider.ChannelProviderFactory
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class NotificationOrchestrator(
    private val channelProviderFactory: ChannelProviderFactory
) {
    fun process(command: CreateNotificationCommand) {
        logger.info { "Processing command for recipient: ${command.recipient}, channels: ${command.channels}" }

        command.channels.forEach { channel ->
            try {
                val channelProvider = channelProviderFactory.getProvider(channel)
                // Delegate the entire command to the provider.
                channelProvider.process(command)
                logger.info { "Successfully delegated to ${channel.name} provider." }
            } catch (e: Exception) {
                // Log and continue, making the system resilient.
                logger.error(e) { "Failed to process channel ${channel.name}. Other channels may still succeed." }
            }
        }
    }
}