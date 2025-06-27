package com.rj.ecommerce_backend.notification.mapper

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.enums.Currency
import com.rj.ecommerce.api.shared.messaging.email.payload.OrderPayload
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.exception.OrderDataInvalidException
import com.rj.ecommerce_backend.user.domain.User
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderNotificationMapper {

    /**
     * Transforms an Order domain entity into a type-safe OrderPayload object.
     */
    fun createOrderPayload(order: Order): OrderPayload {
        fun missing(field: String): Nothing =
            throw OrderDataInvalidException("Order ${order.id} is invalid: missing required field '$field'")

        val orderId = requireNotNull(order.id) {
            "Order must have a non-null ID to create a notification payload."
        }

        val user = order.user ?: missing("user")

        return OrderPayload(
            orderId = orderId.toString(),
            orderNumber = order.id.toString(),
            customer = mapToCustomerInfoDTO(user, ::missing),
            items = mapToMessagingOrderItems(order, ::missing),
            totalAmount = mapToMoney(order.totalAmount, order.currency, "order.totalAmount", ::missing),
            shippingAddress = order.shippingAddress ?: missing("shippingAddress"),
            shippingMethod = order.shippingMethod ?: missing("shippingMethod"),
            paymentMethod = order.paymentMethod ?: missing("paymentMethod"),
            paymentTransactionId = order.paymentTransactionId,
            orderDate = order.orderDate ?: order.createdAt ?: missing("orderDate or createdAt"),
            orderStatus = order.orderStatus
        )
    }

    private fun mapToCustomerInfoDTO(user: User, missing: (String) -> Nothing): CustomerInfoDTO {
        return CustomerInfoDTO(
            id = user.id?.toString() ?: missing("user.id"),
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email.value,
            phoneNumber = user.phoneNumber
        )
    }

    private fun mapToMessagingOrderItems(order: Order, missing: (String) -> Nothing): List<MessagingOrderItemDTO> {
        val orderCurrency = order.currency
        return order.orderItems.map { item ->
            val product = item.product ?: missing("orderItem(${item.id}).product")

            MessagingOrderItemDTO(
                id = item.id?.toString(),
                productId = product.id?.toString() ?: missing("product.id"),
                productName = product.name.value, // Name is non-nullable in Product entity
                quantity = item.quantity,
                unitPrice = mapToMoney(item.price, orderCurrency, "orderItem(${item.id}).price", missing),
                totalPrice = item.price?.let { unitPrice ->
                    val total = unitPrice.multiply(BigDecimal.valueOf(item.quantity.toLong()))
                    mapToMoney(total, orderCurrency, "orderItem(${item.id}).totalPrice", missing)
                } ?: missing("orderItem(${item.id}).price for total calculation")
            )
        }
    }

    private fun mapToMoney(
        amount: BigDecimal?,
        currency: Currency,
        fieldName: String,
        missing: (String) -> Nothing
    ): Money {
        val nonNullAmount = amount ?: missing(fieldName)
        return Money(
            amount = nonNullAmount,
            currencyCode = currency
        )
    }
}