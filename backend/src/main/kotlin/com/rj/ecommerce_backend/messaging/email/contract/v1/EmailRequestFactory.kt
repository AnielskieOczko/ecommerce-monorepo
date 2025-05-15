package com.rj.ecommerce_backend.messaging.email.contract.v1 // Or your chosen Kotlin package

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.core.PhoneNumber
import com.rj.ecommerce.api.shared.core.ZipCode
import com.rj.ecommerce.api.shared.dto.customer.CustomerInfoDTO
import com.rj.ecommerce.api.shared.dto.order.MessagingOrderItemDTO
import com.rj.ecommerce.api.shared.enums.Currency
import com.rj.ecommerce.api.shared.enums.EmailTemplate
import com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO

import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.user.domain.User
// Assuming this is your DOMAIN Address value object, not the DTO
import com.rj.ecommerce_backend.user.valueobject.Address as DomainAddress // Alias to avoid name clash
import org.springframework.stereotype.Component
import java.math.BigDecimal

@Component
class EmailRequestFactory {

    /**
     * Creates an order confirmation email request.
     */
    fun createOrderConfirmationRequest(order: Order): OrderEmailRequestDTO {
        // Ensure order.user, order.id etc. are non-null or handle nullability appropriately
        val user = order.user ?: throw IllegalStateException("Order ${order.id} must have an associated user.")
        val orderIdString = order.id?.toString() ?: throw IllegalStateException("Order must have an ID.")
        val userEmail = user.email?.value ?: throw IllegalStateException("User ${user.id} must have an email.")

        return OrderEmailRequestDTO.create( // Using the factory method from OrderEmailRequestDTO
            to = userEmail,
            template = EmailTemplate.ORDER_CONFIRMATION,
            orderId = orderIdString,
            orderNumber = orderIdString, // Or a separate order number if available
            customer = mapToMessagingCustomerDTO(user), // Use a consistent CustomerDTO type for messaging
            items = mapToMessagingOrderItems(order),
            totalAmount = mapToMoneyDTO(order.totalAmount, order.currency.name),
            shippingAddress = mapToMessagingAddressDTO(),
            shippingMethod = order.shippingMethod,
            paymentMethod = order.paymentMethod,
            orderDate = order.orderDate ?: order.createdAt ?: throw IllegalStateException("Order date or creation date must be present."),
            orderStatus = order.orderStatus ?: throw IllegalStateException("Order status must be present.")
        )
    }

    /**
     * Creates an order shipment email request.
     */
    fun createOrderShipmentRequest(order: Order, trackingNumber: String, trackingUrl: String): OrderEmailRequestDTO {
        val user = order.user ?: throw IllegalStateException("Order ${order.id} must have an associated user.")
        val orderIdString = order.id?.toString() ?: throw IllegalStateException("Order must have an ID.")
        val userEmail = user.email?.value ?: throw IllegalStateException("User ${user.id} must have an email.")

        return OrderEmailRequestDTO.create(
            to = userEmail,
            template = EmailTemplate.ORDER_SHIPMENT,
            orderId = orderIdString,
            orderNumber = orderIdString, // Or a separate order number
            customer = mapToMessagingCustomerDTO(user),
            items = mapToMessagingOrderItems(order),
            totalAmount = mapToMoneyDTO(order.totalAmount, order.currency.name),
            shippingAddress = order.shippingAddress,
            shippingMethod = order.shippingMethod,
            orderDate = order.orderDate ?: order.createdAt ?: throw IllegalStateException("Order date or creation date must be present."),
            orderStatus = order.orderStatus ?: throw IllegalStateException("Order status must be present."),
            additionalData = mapOf( // Kotlin's way to create a map
                "trackingNumber" to trackingNumber,
                "trackingUrl" to trackingUrl
            )
        )
    }

    // --- Mapping Helper Methods ---
    // These now map to DTOs suitable for messaging (could be same as shared API DTOs or specific)

    // Changed to map to a generic CustomerDTO for messaging, not CustomerInfoDTO, unless they are the same
    private fun mapToMessagingCustomerDTO(user: User): CustomerInfoDTO {
        // Assuming CustomerDTO is a data class like:
        // data class CustomerDTO(val id: String?, val firstName: String?, val lastName: String?, val email: String?, val phoneNumber: String?)
        return CustomerInfoDTO(
            id = user.id.toString(),
            firstName = user.firstName, // Assuming User has these properties
            lastName = user.lastName,
            email = user.email.value,
            phoneNumber = user.phoneNumber?.value as PhoneNumber? // Assuming User.phoneNumber is also a value object
        )
    }

    private fun mapToMessagingOrderItems(order: Order): List<MessagingOrderItemDTO> {
        return order.orderItems.map { item ->
            val product = item.product ?: throw IllegalStateException("OrderItem ${item.id} must have an associated product.")
            val productIdString = product.id?.toString() ?: throw IllegalStateException("Product for OrderItem ${item.id} must have an ID.")
            val productNameString = product.name?.value ?: "N/A" // Handle missing product name gracefully or throw

            MessagingOrderItemDTO( // Assuming MessagingOrderItemDTO constructor
                id = item.id?.toString(),
                productId = productIdString,
                productName = productNameString,
                // productSku = product.sku?.value, // If product has SKU and MessagingOrderItemDTO has sku
                quantity = item.quantity,
                unitPrice = mapToMoneyDTO(item.price, order.currency.name),
                totalPrice = item.price?.let { p -> // Calculate total price safely
                    mapToMoneyDTO(p.multiply(BigDecimal.valueOf(item.quantity.toLong())), order.currency.name)
                }
            )
        }
    }

    private fun mapToMoneyDTO(amount: BigDecimal?, currencyCode: String?): Money {
        // Ensure currencyCode is not null if amount is present, or handle default
        val finalCurrencyCode = currencyCode ?: Currency.PLN.name // Default if null
        return Money(
            amount = amount, // MoneyDTO should handle nullable amount if that's valid
            currencyCode = finalCurrencyCode
        )
    }

    // Maps from your DOMAIN Address object to the MESSAGING AddressDTO
    private fun mapToMessagingAddressDTO(domainAddress: DomainAddress): Address {
        // Assuming AddressDTO data class:
        // data class AddressDTO(val street: String?, val city: String?, val zipCode: String?, val country: String?)
        return Address(
            street = domainAddress.street,
            city = domainAddress.city,
            zipCode = domainAddress.zipCode?.value as ZipCode?, // Assuming domainAddress.zipCode is your ZipCode VO
            country = domainAddress.country
        )
    }
}