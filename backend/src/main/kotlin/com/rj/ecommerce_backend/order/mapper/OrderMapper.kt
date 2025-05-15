package com.rj.ecommerce_backend.order.mapper

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.core.ZipCode
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.dto.product.ProductSummaryDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.product.domain.Product // Assuming Product has productName property
import com.rj.ecommerce.api.shared.enums.Currency // Use this one
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderMapper {

    fun toDto(order: Order?): OrderDTO? {
        return order?.let { o ->
            val orderItemDTOs = o.orderItems.mapNotNull { orderItem -> toDto(orderItem) }

            val shippingAddressDTO = o.shippingAddress?.let { sa ->
                Address(
                    street = sa.street,
                    city = sa.city,
                    zipCode = sa.zipCode?.let { ZipCode(it.value) },
                    country = sa.country
                )
            }

            OrderDTO(
                id = o.id,
                userId = o.user?.id,
                customerEmail = o.user?.email?.value,
                items = orderItemDTOs,
                totalAmount = o.totalAmount?.let { Money(it, o.currency) },
                shippingAddress = shippingAddressDTO,
                shippingMethod = o.shippingMethod,
                paymentMethod = o.paymentMethod,
                orderStatus = o.orderStatus,
                paymentStatus = o.paymentStatus,
                paymentTransactionId = o.paymentTransactionId,
                orderDate = o.orderDate,
                checkoutSessionUrl = o.checkoutSessionUrl,
                checkoutSessionExpiresAt = o.checkoutSessionExpiresAt,
                receiptUrl = o.receiptUrl
            )
        }
    }

    fun toDto(orderItem: OrderItem?): OrderItemDTO? {
        return orderItem?.let { oi ->
            val productEntity: Product? = oi.product

            val productNameValue: String? = productEntity?.name?.value
            if (productNameValue == null && productEntity != null) {
                // Log warning or decide on a default if critical
                // For now, let it be null if product/name is null
            }


            val productSummary = ProductSummaryDTO(
                id = productEntity?.id,
                sku = null,
                name = productNameValue,
                unitPrice = oi.price?.let { price ->
                    Money(
                        amount = price,
                        currencyCode = oi.order?.currency ?: Currency.PLN
                    )
                }
            )

            OrderItemDTO(
                product = productSummary,
                quantity = oi.quantity,
                lineTotal = getLineTotal(oi)
            )
        }
    }

    fun toEntity(orderDTO: OrderDTO?): Order? {
        return orderDTO?.let { dto ->
            val shippingAddressEntity = dto.shippingAddress?.let { saDto ->
                Address(
                    street = saDto.street,
                    city = saDto.city,
                    zipCode = saDto.zipCode,
                    country = saDto.country
                )
            }

            Order(
                id = dto.id,
                user = null,
                totalAmount = dto.totalAmount?.amount,
                currency = dto.totalAmount?.currencyCode?.let {
                    try {
                        Currency.valueOf(it.name)
                    } catch (e: IllegalArgumentException) {
                        Currency.PLN
                    }
                } ?: Currency.PLN,
                shippingAddress = shippingAddressEntity,
                shippingMethod = dto.shippingMethod,
                paymentMethod = dto.paymentMethod,
                paymentTransactionId = dto.paymentTransactionId,
                checkoutSessionUrl = dto.checkoutSessionUrl,
                checkoutSessionExpiresAt = dto.checkoutSessionExpiresAt,
                receiptUrl = dto.receiptUrl,
                paymentStatus = dto.paymentStatus,
                orderStatus = dto.orderStatus
            ).also { orderEntity ->
                dto.items.forEach { itemDto ->
                    toEntity(itemDto)?.let { orderItemEntity ->
                        orderEntity.addOrderItem(orderItemEntity)
                    }
                }
            }
        }
    }

    fun toEntity(orderItemDTO: OrderItemDTO?): OrderItem? {
        return orderItemDTO?.let { dto ->
            OrderItem(
                id = null,
                quantity = dto.quantity,
                price = dto.product.unitPrice?.amount
            )
        }
    }

    companion object {
        private fun getLineTotal(orderItem: OrderItem): Money {
            val itemPrice = orderItem.price ?: BigDecimal.ZERO
            val quantity = BigDecimal.valueOf(orderItem.quantity.toLong())
            val lineAmount = itemPrice.multiply(quantity)

            // Handle null order or null currency on order
            val currencyCode = orderItem.order?.currency ?: Currency.PLN

            return Money(
                amount = lineAmount,
                currencyCode = currencyCode
            )
        }
    }
}