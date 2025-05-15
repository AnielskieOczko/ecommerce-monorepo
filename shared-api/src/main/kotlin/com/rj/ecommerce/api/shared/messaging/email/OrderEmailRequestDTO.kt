package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.core.ShippingAddressDTO
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime
import java.util.UUID

data class OrderEmailRequestDTO(
    override val messageId: String,
    override val version: String,
    override val to: String,
    override val subject: String, // Will be set by companion object or init
    override val template: EmailTemplate,
    val orderId: String,
    val orderNumber: String? = null,
    val customer: CustomerInfoDTO? = null, // Make nullable if optional for some templates
    @field:NotEmpty // Ensure list is not empty
    val items: List<MessagingOrderItemDTO>, // Use the messaging-specific OrderItemDTO
    val totalAmount: Money,
    val shippingAddress: ShippingAddressDTO? = null,
    val shippingMethod: ShippingMethod? = null, // Corrected import
    val paymentMethod: PaymentMethod? = null,
    val paymentTransactionId: String? = null,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus,
    val additionalData: Map<String, Any> = emptyMap(),
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : EcommerceEmailRequest {

    init {
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(version.isNotBlank()) { "Version cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(orderId.isNotBlank()) { "Order ID cannot be blank" }
        require(items.isNotEmpty()) { "Order must have at least one item" }
        // Subject is handled by factory method or constructor logic
    }


    override fun getTemplateData(): Map<String, Any> {
        return buildMap {
            put("orderId", orderId)
            orderNumber?.let { put("orderNumber", it) }
            customer?.let { put("customer", it) }
            put("items", items)
            put("totalAmount", totalAmount)
            shippingAddress?.let { put("shippingAddress", it) }
            shippingMethod?.let { put("shippingMethod", it.name) } // Send enum name
            paymentMethod?.let { put("paymentMethod", it.name) }   // Send enum name
            paymentTransactionId?.let { put("paymentTransactionId", it) }
            put("orderDate", orderDate)
            put("orderStatus", orderStatus.name) // Send enum name
            putAll(additionalData)
        }
    }

    companion object {
        private fun generateSubject(template: EmailTemplate, orderNumber: String?): String {
            val orderRef = if (!orderNumber.isNullOrBlank()) " #$orderNumber" else ""
            return when (template) {
                EmailTemplate.ORDER_CONFIRMATION -> "Your Order$orderRef Confirmation"
                EmailTemplate.ORDER_SHIPMENT -> "Your Order$orderRef Has Been Shipped"
                EmailTemplate.ORDER_CANCELLED -> "Your Order$orderRef Has Been Cancelled"
                EmailTemplate.ORDER_REFUNDED -> "Your Order$orderRef Has Been Refunded"
                else -> "Information About Your Order$orderRef" // Handles CUSTOMER_WELCOME etc. if template is passed
            }
        }

        // Factory function to ensure subject is generated
        @JvmStatic
        fun create(
            to: String,
            template: EmailTemplate,
            orderId: String,
            items: List<MessagingOrderItemDTO>,
            totalAmount: Money,
            orderDate: LocalDateTime,
            orderStatus: OrderStatus,
            messageId: String = UUID.randomUUID().toString(),
            version: String = "1.0",
            orderNumber: String? = null,
            customer: CustomerInfoDTO? = null,
            shippingAddress: ShippingAddressDTO? = null,
            shippingMethod: ShippingMethod? = null,
            paymentMethod: PaymentMethod? = null,
            paymentTransactionId: String? = null,
            additionalData: Map<String, Any> = emptyMap(),
            timestamp: LocalDateTime = LocalDateTime.now(),
            subjectOverride: String? = null // Allow overriding the generated subject
        ): OrderEmailRequestDTO {
            val finalSubject = subjectOverride ?: generateSubject(template, orderNumber)
            return OrderEmailRequestDTO(
                messageId, version, to, finalSubject, template, orderId, orderNumber,
                customer, items, totalAmount, shippingAddress, shippingMethod, paymentMethod,
                paymentTransactionId, orderDate, orderStatus, additionalData, timestamp
            )
        }
    }
}