package com.rj.ecommerce_backend.order.mapper

import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.dto.product.ProductSummaryDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.product.domain.Product
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderMapper(
) {

    /**
     * Maps an Order domain entity to its corresponding DTO.
     * This method now uses .map, relying on the item mapper to never return null.
     */
    fun toDto(order: Order): OrderDTO {
        // The call to .map is now safe because our item mapper guarantees a non-null result.
        val orderItemDTOs = order.orderItems.map { toDto(it) }

        return OrderDTO(
            id = order.id,
            userId = order.user?.id,
            customerEmail = order.user?.email?.value,
            items = orderItemDTOs, // This is now a non-nullable list
            totalAmount = order.totalAmount?.let { Money(it, order.currency) },
            shippingAddress = order.shippingAddress,
            shippingMethod = order.shippingMethod,
            paymentMethod = order.paymentMethod,
            orderStatus = order.orderStatus,
            paymentStatus = order.paymentStatus,
            paymentTransactionId = order.paymentTransactionId,
            orderDate = order.orderDate,
            checkoutSessionUrl = order.checkoutSessionUrl,
            checkoutSessionExpiresAt = order.checkoutSessionExpiresAt,
            receiptUrl = order.receiptUrl
        )
    }

    /**
     * Maps an OrderItem domain entity to its corresponding DTO.
     * This method now accepts a non-nullable OrderItem and is guaranteed to return
     * a non-nullable OrderItemDTO, or it will throw an exception if the entity data is invalid.
     */
    private fun toDto(orderItem: OrderItem): OrderItemDTO {
        // Use requireNotNull to enforce data integrity. If any of these are null,
        // it's a data corruption issue that should fail fast.
        val product = requireNotNull(orderItem.product) { "Product cannot be null for OrderItem ID: ${orderItem.id}" }
        val productId = requireNotNull(product.id) { "Product ID cannot be null for OrderItem ID: ${orderItem.id}" }
        val productName = requireNotNull(product.name.value) { "Product name cannot be null for Product ID: $productId" }
        val unitPrice = requireNotNull(orderItem.price) { "Price cannot be null for OrderItem ID: ${orderItem.id}" }
        val orderCurrency = requireNotNull(orderItem.order?.currency) { "Order currency cannot be null for OrderItem ID: ${orderItem.id}" }

        val productSummary = ProductSummaryDTO(
            id = productId,
            name = productName,
            unitPrice = Money(unitPrice, orderCurrency)
        )

        val lineTotalAmount = unitPrice.multiply(BigDecimal.valueOf(orderItem.quantity.toLong()))
        val lineTotal = Money(lineTotalAmount, orderCurrency)

        return OrderItemDTO(
            product = productSummary,
            quantity = orderItem.quantity,
            lineTotal = lineTotal
        )
    }


    /**
     * Safely updates a managed Order entity with data from a DTO.
     * This method assumes the entity has been fetched from the database within a transaction.
     * It does NOT save the entity; that is the service's responsibility.
     */
    fun updateEntityFromDto(entity: Order, dto: OrderDTO, products: Map<Long, Product>) {
        // ... update simple fields like shippingAddress, status, etc. ...

        updateOrderItemsFromDto(entity, dto.items, products)
    }

    private fun updateOrderItemsFromDto(
        order: Order,
        itemDtos: List<OrderItemDTO>,
        productsById: Map<Long, Product>
    ) {
        order.orderItems.clear()

        itemDtos.forEach { itemDto ->
            // Look up the product from the provided map.
            // If it's not there, it's a logic error in the service, which should have validated it.
            val product = requireNotNull(productsById[itemDto.product.id]) {
                "Logic error: Product with ID ${itemDto.product.id} was not pre-fetched by the service."
            }

            val newOrderItem = OrderItem(
                order = order,
                product = product,
                quantity = itemDto.quantity,
                price = product.unitPrice.amount
            )
            order.addOrderItem(newOrderItem)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {  }
    }
}