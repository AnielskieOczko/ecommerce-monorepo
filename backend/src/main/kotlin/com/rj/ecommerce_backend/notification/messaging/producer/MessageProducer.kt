package com.rj.ecommerce_backend.notification.messaging.producer

import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationDeliveryReceipt
import com.rj.notification_service.config.AppProperties
import com.rj.notification_service.messaging.exception.MessagePublishException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Component

@Component
class MessageProducer(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: AppProperties
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    fun <T : Any> sendMessage(exchange: String, routingKey: String, message: T, correlationId: String?) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, message) { msg ->
                if (!correlationId.isNullOrEmpty()) {
                    msg.messageProperties.correlationId = correlationId
                }
                msg
            }

            logger.info { "Sent message to exchange: $exchange, routing key: $routingKey, correlationId: $correlationId" }
            logger.debug { "Message details: $message" }

        } catch (e: Exception) {
            logger.error(e) { "Failed to send message to exchange: $exchange, routing key: $routingKey" }
            throw MessagePublishException("Failed to publish message", e)
        }
    }

    fun sendDeliveryReceipt(receipt: NotificationDeliveryReceipt) {
        val receiptConfig = rabbitMQProperties.rabbitmq.notificationReceipt
        sendMessage(
            exchange = receiptConfig.exchange,
            routingKey = receiptConfig.routingKey,
            message = receipt,
            correlationId = receipt.correlationId
        )
    }
}