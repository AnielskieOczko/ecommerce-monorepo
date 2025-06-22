package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.dto.payment.PaymentOptionDTO
import com.rj.ecommerce.api.shared.messaging.payment.GetPaymentOptionsReplyDTO
import com.rj.ecommerce.api.shared.messaging.payment.GetPaymentOptionsRequestDTO
import com.rj.ecommerce_backend.messaging.config.RabbitMQProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class PaymentOptionsService(
    private val rabbitTemplate: RabbitTemplate,
    private val rabbitMQProperties: RabbitMQProperties // Your type-safe properties class
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // The response is highly cacheable to avoid messaging overhead on every request.
    @Cacheable("payment-options")
    fun getAvailablePaymentOptions(): List<PaymentOptionDTO> {
        logger.info { "Requesting payment options from Payment Microservice." }

        val request = GetPaymentOptionsRequestDTO()
        val requestConfig = rabbitMQProperties.paymentOptionsRequest
        val replyConfig = rabbitMQProperties.paymentOptionsReply

        try {
            // RabbitTemplate's sendAndReceive method handles the correlation and reply logic.
            // It will send to the request exchange/routingKey and listen on the reply queue.
            val reply = rabbitTemplate.convertSendAndReceive(
                requestConfig.exchange,
                requestConfig.routingKey,
                request
            ) as? GetPaymentOptionsReplyDTO // Cast the generic object response

            if (reply == null) {
                logger.error { "Did not receive a reply for payment options request. Timed out?" }
                throw IllegalStateException("Failed to get payment options from the payment service.")
            }

            logger.info { "Successfully received ${reply.options.size} payment options." }
            return reply.options

        } catch (e: Exception) {
            logger.error(e) { "Error during request-reply for payment options." }
            // Return an empty list or re-throw a specific exception.
            // For a discovery endpoint, failing gracefully is often better.
            return emptyList()
        }
    }
}