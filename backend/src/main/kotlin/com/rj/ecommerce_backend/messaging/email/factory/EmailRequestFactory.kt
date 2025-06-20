package com.rj.ecommerce_backend.messaging.email.factory

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.enums.Currency
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.OrderDataInvalidException
import com.rj.ecommerce_backend.user.domain.User
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EmailRequestFactory {

    fun createOrderConfirmationRequest(order: Order): OrderEmailRequestDTO {
        return createBaseOrderEmailRequest(order, EmailTemplate.ORDER_CONFIRMATION)
    }

    fun createOrderShipmentRequest(order: Order, trackingNumber: String, trackingUrl: String): OrderEmailRequestDTO {
        val additionalData = mapOf(
            "trackingNumber" to trackingNumber,
            "trackingUrl" to trackingUrl
        )
        return createBaseOrderEmailRequest(order, EmailTemplate.ORDER_SHIPMENT, additionalData)
    }

    private fun createBaseOrderEmailRequest(
        order: Order,
        template: EmailTemplate,
        additionalData: Map<String, Any> = emptyMap()
    ): OrderEmailRequestDTO {
        fun missing(field: String): Nothing = throw OrderDataInvalidException("Order ${order.id} is invalid: missing required field '$field'")

        val user = order.user ?: missing("user")
        val orderIdString = order.id?.toString() ?: missing("id")
        val userEmail = user.email?.value ?: missing("user.email") // Made this check safer

        return OrderEmailRequestDTO.Companion.create(
            to = userEmail,
            template = template,
            orderId = orderIdString,
            orderNumber = orderIdString,
            customer = mapToCustomerInfoDTO(user),
            items = mapToMessagingOrderItems(order),
            totalAmount = mapToMoney(order.totalAmount, order.currency.name),
            shippingAddress = order.shippingAddress,
            shippingMethod = order.shippingMethod,
            paymentMethod = order.paymentMethod,
            orderDate = order.orderDate ?: order.createdAt ?: missing("orderDate or createdAt"),
            orderStatus = order.orderStatus ?: missing("orderStatus"),
            additionalData = additionalData,
            correlationId = TODO(),
            paymentTransactionId = TODO(),
            subjectOverride = TODO()
        )
    }

    private fun mapToCustomerInfoDTO(user: User): CustomerInfoDTO {
        return CustomerInfoDTO(
            id = user.id.toString(),
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email.value,
            phoneNumber = user.phoneNumber // Correctly map from the value object
        )
    }

    private fun mapToMessagingOrderItems(order: Order): List<MessagingOrderItemDTO> {
        val currencyName = order.currency.name
        return order.orderItems.map { item ->
            fun missing(field: String): Nothing = throw OrderDataInvalidException("OrderItem ${item.id} is invalid: missing '$field'")
            val product = item.product ?: missing("product")

            MessagingOrderItemDTO(
                id = item.id?.toString(),
                productId = product.id?.toString() ?: missing("product.id"),
                productName = product.name?.value ?: "Product Name Unavailable",
                quantity = item.quantity,
                unitPrice = mapToMoney(item.price, currencyName),
                totalPrice = item.price?.let { unitPrice ->
                    val total = unitPrice.multiply(BigDecimal.valueOf(item.quantity.toLong()))
                    mapToMoney(total, currencyName)
                }
            )
        }
    }

    private fun mapToMoney(amount: BigDecimal?, currencyCode: String): Money {
        return Money(
            amount = amount ?: BigDecimal.ZERO,
            // The Money DTO expects a String code, not an Enum. This fixes a bug.
            currencyCode = Currency.valueOf(currencyCode)
        )
    }
}