package order.mapper

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.dto.order.OrderItemDTO
import com.rj.ecommerce.api.shared.dto.product.ProductSummary
import com.rj.ecommerce.api.shared.enums.PaymentStatus
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.order.enums.Currency
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class OrderMapper {

    fun toDto(order: Order?): OrderDTO? {
        if (order == null) {
            return null
        }

        val orderItemDTOs = order.orderItems.map { orderItem: OrderItem -> toDto(orderItem) }

        val addressDTO = Address(
            order.shippingAddress.street,
            order.shippingAddress.city,
            order.shippingAddress.zipCode,
            order.shippingAddress.country
        )

        return OrderDTO(
            id = order.id,
            userId = order.user.id,
            customerEmail = order.user.email.value,
            items = orderItemDTOs,
            totalAmount = Money(order.totalPrice, order.currency.name),
            shippingAddress = addressDTO,
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

    fun toDto(orderItem: OrderItem?): OrderItemDTO? {
        if (orderItem == null) {
            return null
        }
        val product = orderItem.product
        val productName: String =
            (if (product != null && product.getProductName() != null) product.getProductName().value else null)!!

        val productSummary = ProductSummary(
            id = product.id,
            sku = null,
            name = productName,
            unitPrice = Money(
                amount = orderItem.price,
                currencyCode = orderItem.order.currency.name)
        )

        return OrderItemDTO(
            product = productSummary,
            quantity = orderItem.quantity,
            lineTotal = getLineTotal(orderItem)
        )
    }

    fun toEntity(orderDTO: OrderDTO?): Order? {
        if (orderDTO == null) {
            return null
        }

        // Create Address value object from AddressDTO
        val address = Address(
            street = orderDTO.shippingAddress.street,
            city = orderDTO.shippingAddress.city,
            zipCode = orderDTO.shippingAddress.zipCode,
            country = orderDTO.shippingAddress.country
        )

        return Order.builder()
            .id(orderDTO.id)
            .user(null)
            .totalPrice(orderDTO.totalAmount.amount)
            .currency(Currency.valueOf(orderDTO.totalAmount.currencyCode))
            .shippingAddress(address)
            .shippingMethod(orderDTO.shippingMethod)
            .paymentMethod(orderDTO.paymentMethod)
            .paymentTransactionId(orderDTO.paymentTransactionId)
            .checkoutSessionUrl(orderDTO.checkoutSessionUrl)
            .receiptUrl(orderDTO.receiptUrl)
            .paymentStatus(orderDTO.paymentStatus)
            .orderDate(orderDTO.orderDate)
            .orderStatus(orderDTO.orderStatus)
            .build()
    }

    fun toEntity(orderItemDTO: OrderItemDTO?): OrderItem? {
        if (orderItemDTO == null) {
            return null
        }

        return OrderItem.builder()
            .id(orderItemDTO.product.id)
            .quantity(orderItemDTO.quantity)
            .price(orderItemDTO.lineTotal.amount)
            .build()
    }

    companion object {
        private fun getLineTotal(orderItem: OrderItem): Money {

            val lineAmount = orderItem.price.multiply(
                BigDecimal.valueOf(orderItem.quantity.toLong()))

            return Money(
                amount = lineAmount,
                currencyCode = orderItem.order.currency.name
            )
        }
    }
}
