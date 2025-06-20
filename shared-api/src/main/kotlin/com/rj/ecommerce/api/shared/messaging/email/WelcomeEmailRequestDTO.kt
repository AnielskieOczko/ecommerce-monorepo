// AFTER
package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import java.time.LocalDateTime
import java.util.UUID

data class WelcomeEmailRequestDTO(
    override val messageId: String,
    override val correlationId: String,
    override val version: String,
    override val to: String,
    override val subject: String,
    override val template: EmailTemplate,
    override val timestamp: LocalDateTime,
    val customerName: String,
    val couponCode: String? = null,
    val additionalData: Map<String, Any> = emptyMap()
) : EcommerceEmailRequest {

    init {
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(correlationId.isNotBlank()) { "Correlation ID cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(customerName.isNotBlank()) { "Customer name cannot be blank" }
    }

    override fun getTemplateData(): Map<String, Any> {
        return buildMap {
            put("customerName", customerName)
            couponCode?.let { put("couponCode", it) }
            putAll(additionalData)
        }
    }

    companion object {
        @JvmStatic
        fun create(
            to: String,
            customerName: String,
            correlationId: String,
            couponCode: String? = null,
            additionalData: Map<String, Any> = emptyMap()
        ): WelcomeEmailRequestDTO {
            return WelcomeEmailRequestDTO(
                messageId = UUID.randomUUID().toString(),
                correlationId = correlationId,
                version = MessageVersioning.CURRENT_VERSION,
                to = to,
                subject = "Welcome to Our Store!",
                template = EmailTemplate.CUSTOMER_WELCOME,
                timestamp = LocalDateTime.now(),
                customerName = customerName,
                couponCode = couponCode,
                additionalData = additionalData
            )
        }
    }
}