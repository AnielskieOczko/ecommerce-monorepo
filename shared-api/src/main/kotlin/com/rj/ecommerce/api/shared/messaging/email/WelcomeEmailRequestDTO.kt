package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.enums.EmailTemplate
import java.time.LocalDateTime
import java.util.UUID

data class WelcomeEmailRequestDTO(
    // @field:NotBlank // If using Bean Validation
    override val messageId: String, // Non-nullable by type
    override val version: String,   // Non-nullable by type
    override val to: String,        // Non-nullable by type
    override val subject: String = "Welcome to Our Store!", // Default value in constructor
    val customerName: String, // Non-nullable by type
    val couponCode: String? = null, // Nullable with default
    val additionalData: Map<String, Any> = emptyMap(), // Use Any for Object, default to emptyMap
    override val timestamp: LocalDateTime = LocalDateTime.now(), // Default value
    override val template: EmailTemplate
) : EcommerceEmailRequest {

    init {
        // require is Kotlin's way to throw IllegalArgumentException
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(version.isNotBlank()) { "Version cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(customerName.isNotBlank()) { "Customer name cannot be blank" }
        // subject, timestamp, additionalData have defaults or are handled by constructor.
    }

    override fun getTemplateData(): Map<String, Any> { // Use Any for Object
        return buildMap { // Kotlin's scope function for building maps
            put("customerName", customerName)
            couponCode?.let { put("couponCode", it) } // Add only if not null
            putAll(additionalData) // additionalData is already defaulted to emptyMap if null
        }
    }

    companion object {
        @JvmStatic // For Java interop if needed
        fun defaultBuilder(
            to: String, // Mandatory fields for a minimal valid object
            customerName: String,
            // Other optional fields with defaults or null
            subject: String = "Welcome to Our Store!",
            couponCode: String? = null,
            additionalData: Map<String, Any> = emptyMap()
        ): WelcomeEmailRequestDTO {
            return WelcomeEmailRequestDTO(
                messageId = UUID.randomUUID().toString(),
                version = "1.0",
                to = to,
                subject = subject,
                customerName = customerName,
                couponCode = couponCode,
                additionalData = additionalData,
                timestamp = LocalDateTime.now(),
                template = EmailTemplate.CUSTOMER_WELCOME
            )
        }
    }
}