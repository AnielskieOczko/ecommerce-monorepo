package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import java.time.LocalDateTime
import java.util.UUID

data class PaymentEmailRequestDTO(
    // Properties from the EcommerceEmailRequest interface
    override val messageId: String = UUID.randomUUID().toString(),
    override val version: String,
    override val to: String,
    override val template: EmailTemplate,
    override val timestamp: LocalDateTime = LocalDateTime.now(),

    // Properties specific to this DTO
    val orderId: Long, // Changed to Long for consistency
    val paymentId: String? = null,
    val paymentStatus: String,
    val paymentAmount: Money? = null,
    val additionalData: Map<String, Any> = emptyMap()

) : EcommerceEmailRequest {

    // 1. The 'subject' is now a computed property that implements the interface requirement.
    // It's calculated automatically based on the template and orderId.
    override val subject: String
        get() = when (template) {
            EmailTemplate.PAYMENT_CONFIRMATION -> "Payment Confirmed - Order #$orderId"
            EmailTemplate.PAYMENT_FAILED -> "Payment Failed - Order #$orderId"
            EmailTemplate.PAYMENT_ERROR_ADMIN -> "Payment Processing Error - Order #$orderId"
            EmailTemplate.PAYMENT_ERROR_CUSTOMER -> "Payment Processing Update - Order #$orderId"
            else -> "Payment Information - Order #$orderId" // A sensible default
        }

    init {
        // Validation remains the same - this is a good practice.
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(version.isNotBlank()) { "Version cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(paymentStatus.isNotBlank()) { "Payment status cannot be blank" }
    }

    override fun getTemplateData(): Map<String, Any> {
        // buildMap is an excellent, idiomatic way to do this. No changes needed.
        return buildMap {
            put("orderId", orderId)
            paymentId?.let { put("paymentId", it) }
            put("paymentStatus", paymentStatus)
            paymentAmount?.let { put("paymentAmount", it) }
            putAll(additionalData)
        }
    }

    // 2. The companion object is now empty as the factory function is no longer needed.
    // It can be removed entirely if not used for anything else.
    companion object {
        // No factory method needed. Use the primary constructor with named arguments.
    }
}