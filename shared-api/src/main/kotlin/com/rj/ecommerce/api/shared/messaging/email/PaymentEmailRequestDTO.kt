package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import java.time.LocalDateTime
import java.util.UUID

data class PaymentEmailRequestDTO(
    override val messageId: String,
    override val version: String,
    override val to: String,
    override val subject: String,
    override val template: EmailTemplate,
    val orderId: String,
    val paymentId: String? = null,
    val paymentStatus: String, // String, not enum, as per original. Could be enum type too.
    val paymentAmount: Money? = null,
    val additionalData: Map<String, Any> = emptyMap(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : EcommerceEmailRequest {

    init {
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(version.isNotBlank()) { "Version cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(orderId.isNotBlank()) { "Order ID cannot be blank" }
        require(paymentStatus.isNotBlank()) { "Payment status cannot be blank" }
    }

    override fun getTemplateData(): Map<String, Any> {
        return buildMap {
            put("orderId", orderId)
            paymentId?.let { put("paymentId", it) }
            put("paymentStatus", paymentStatus)
            paymentAmount?.let { put("paymentAmount", it) }
            putAll(additionalData)
        }
    }

    companion object {
        private fun generateSubject(template: EmailTemplate, orderId: String): String {
            return when (template) {
                EmailTemplate.PAYMENT_CONFIRMATION -> "Payment Confirmed - Order #$orderId"
                EmailTemplate.PAYMENT_FAILED -> "Payment Failed - Order #$orderId"
                EmailTemplate.PAYMENT_ERROR_ADMIN -> "Payment Processing Error - Order #$orderId"
                EmailTemplate.PAYMENT_ERROR_CUSTOMER -> "Payment Processing Update - Order #$orderId"
                else -> "Payment Information - Order #$orderId"
            }
        }

        // Factory function
        @JvmStatic
        fun create(
            to: String,
            template: EmailTemplate,
            orderId: String,
            paymentStatus: String, // Consider using PaymentStatus enum if appropriate
            messageId: String = UUID.randomUUID().toString(),
            version: String = "1.0",
            paymentId: String? = null,
            paymentAmount: Money? = null,
            additionalData: Map<String, Any> = emptyMap(),
            timestamp: LocalDateTime = LocalDateTime.now(),
            subjectOverride: String? = null
        ): PaymentEmailRequestDTO {
            val finalSubject = subjectOverride ?: generateSubject(template, orderId)
            return PaymentEmailRequestDTO(
                messageId, version, to, finalSubject, template, orderId, paymentId,
                paymentStatus, paymentAmount, additionalData, timestamp
            )
        }
    }
}