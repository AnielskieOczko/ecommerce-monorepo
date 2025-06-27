package com.rj.ecommerce_backend.notification.service

import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.messaging.email.EmailRequest
import com.rj.ecommerce.api.shared.messaging.email.MessageEnvelope
import com.rj.ecommerce_backend.messaging.email.producer.EmailMessageProducer
import com.rj.ecommerce_backend.notification.Notification
import com.rj.ecommerce_backend.notification.NotificationContext
import com.rj.ecommerce_backend.notification.mapper.OrderNotificationMapper
import org.springframework.stereotype.Component

@Component
class EmailNotificationProvider(
    private val orderNotificationMapper: OrderNotificationMapper,
    private val emailMessageProducer: EmailMessageProducer
) : NotificationProvider {

    override fun getChannel(): NotificationChannel = NotificationChannel.EMAIL

    override fun send(notification: Notification) {
        when (val context = notification.context) {
            is NotificationContext.OrderContext -> {
                // 1. Create the specific payload object from the order context
                val payload = orderNotificationMapper.createOrderEmailRequestPayload(context.order)

                // 2. Generate the subject line (logic now lives here)
                val subject = generateSubject(notification.template, payload.orderNumber)

                // 3. Create the message envelope with all metadata
                val envelope = MessageEnvelope(
                    to = notification.recipient,
                    subject = subject,
                    template = notification.template,
                    correlationId = notification.correlationId
                )

                // 4. Compose the final, generic request and send it
                val emailRequest = EmailRequest(envelope, payload)
                emailMessageProducer.send(emailRequest)
            }
            is NotificationContext.EmptyContext -> {
                throw NotImplementedError("EmailNotificationProvider cannot send an email with an EmptyContext.")
            }
        }
    }

    private fun generateSubject(template: NotificationTemplate, orderNumber: String?): String {
        val orderRef = if (!orderNumber.isNullOrBlank()) " #$orderNumber" else ""
        return when (template) {
            NotificationTemplate.ORDER_CONFIRMATION -> "Your Order$orderRef Confirmation"
            NotificationTemplate.ORDER_SHIPMENT -> "Your Order$orderRef Has Been Shipped"
            NotificationTemplate.ORDER_CANCELLED -> "Your Order$orderRef Has Been Cancelled"
            NotificationTemplate.ORDER_REFUNDED -> "Your Order$orderRef Has Been Refunded"
            else -> "Information About Your Order$orderRef"
        }
    }
}


