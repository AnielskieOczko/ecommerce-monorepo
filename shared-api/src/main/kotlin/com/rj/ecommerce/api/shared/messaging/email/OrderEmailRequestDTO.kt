package com.rj.ecommerce.api.shared.messaging.email

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.enums.OrderStatus
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod
import com.rj.ecommerce.api.shared.messaging.contract.MessageVersioning
import jakarta.validation.constraints.NotEmpty
import java.time.LocalDateTime
import java.util.UUID

data class OrderEmailRequestDTO(
    // --- Interface Properties ---
    override val messageId: String,
    override val correlationId: String,
    override val version: String,
    override val to: String,
    override val subject: String,
    override val template: EmailTemplate,
    override val timestamp: LocalDateTime,

    // --- DTO-Specific Properties ---
    val orderId: String,
    val orderNumber: String? = null,
    val customer: CustomerInfoDTO? = null,
    @field:NotEmpty
    val items: List<MessagingOrderItemDTO>,
    val totalAmount: Money,
    val shippingAddress: Address? = null,
    val shippingMethod: ShippingMethod? = null,
    val paymentMethod: PaymentMethod? = null,
    val paymentTransactionId: String? = null,
    val orderDate: LocalDateTime,
    val orderStatus: OrderStatus,
    val additionalData: Map<String, Any> = emptyMap()
) : EcommerceEmailRequest {

    init {
        require(messageId.isNotBlank()) { "Message ID cannot be blank" }
        require(correlationId.isNotBlank()) { "Correlation ID cannot be blank" } // 2. ADDED: Validation for correlationId
        require(version.isNotBlank()) { "Version cannot be blank" }
        require(to.isNotBlank()) { "Recipient email cannot be blank" }
        require(orderId.isNotBlank()) { "Order ID cannot be blank" }
        require(items.isNotEmpty()) { "Order must have at least one item" }
    }

    override fun getTemplateData(): Map<String, Any> {
        return buildMap {
            put("orderId", orderId)
            orderNumber?.let { put("orderNumber", it) }
            customer?.let { put("customer", it) }
            put("items", items)
            put("totalAmount", totalAmount)
            shippingAddress?.let { put("shippingAddress", it) }
            shippingMethod?.let { put("shippingMethod", it.name) }
            paymentMethod?.let { put("paymentMethod", it.name) }
            paymentTransactionId?.let { put("paymentTransactionId", it) }
            put("orderDate", orderDate)
            put("orderStatus", orderStatus.name)
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
                else -> "Information About Your Order$orderRef"
            }
        }

        @JvmStatic
        fun create(
            // --- Required Parameters ---
            to: String,
            correlationId: String, // 3. ADDED: correlationId is a required parameter for the factory
            template: EmailTemplate,
            orderId: String,
            items: List<MessagingOrderItemDTO>,
            totalAmount: Money,
            orderDate: LocalDateTime,
            orderStatus: OrderStatus,
            // --- Optional Parameters ---
            orderNumber: String? = null,
            customer: CustomerInfoDTO? = null,
            shippingAddress: Address? = null,
            shippingMethod: ShippingMethod? = null,
            paymentMethod: PaymentMethod? = null,
            paymentTransactionId: String? = null,
            additionalData: Map<String, Any> = emptyMap(),
            subjectOverride: String? = null
        ): OrderEmailRequestDTO {
            val finalSubject = subjectOverride ?: generateSubject(template, orderNumber)

            return OrderEmailRequestDTO(
                messageId = UUID.randomUUID().toString(),
                correlationId = correlationId,
                version = MessageVersioning.CURRENT_VERSION,
                to = to,
                subject = finalSubject,
                template = template,
                timestamp = LocalDateTime.now(),
                orderId = orderId,
                orderNumber = orderNumber,
                customer = customer,
                items = items,
                totalAmount = totalAmount,
                shippingAddress = shippingAddress,
                shippingMethod = shippingMethod,
                paymentMethod = paymentMethod,
                paymentTransactionId = paymentTransactionId,
                orderDate = orderDate,
                orderStatus = orderStatus,
                additionalData = additionalData
            )
        }
    }
}