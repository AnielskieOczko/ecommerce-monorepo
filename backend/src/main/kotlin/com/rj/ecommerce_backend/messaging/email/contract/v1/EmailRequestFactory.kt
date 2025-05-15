package com.rj.ecommerce_backend.messaging.email.contract.v1

import com.rj.ecommerce_backend.messaging.email.contract.v1.common.AddressDTO

@org.springframework.stereotype.Component
class EmailRequestFactory {
    /**
     * Create an order confirmation email request
     */
    fun createOrderConfirmationRequest(order: com.rj.ecommerce_backend.order.domain.Order): com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO {
        return com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO.builder()
            .messageId(java.util.UUID.randomUUID().toString())
            .version("1.0")
            .to(order.user!!.getEmail().value)
            .template(com.rj.ecommerce.api.shared.enums.EmailTemplate.ORDER_CONFIRMATION)
            .orderId(order.id.toString())
            .orderNumber(order.id.toString())
            .customer(mapCustomer(order.user!!))
            .items(mapOrderItems(order))
            .totalAmount(mapMoney(order.totalAmount, order.currency.name))
            .shippingAddress(mapAddress(order.shippingAddress))
            .shippingMethod(order.shippingMethod)
            .paymentMethod(order.paymentMethod)
            .orderDate(order.createdAt)
            .orderStatus(order.orderStatus)
            .build()
    }

    /**
     * Create an order shipment email request
     */
    fun createOrderShipmentRequest(
        order: com.rj.ecommerce_backend.order.domain.Order,
        trackingNumber: kotlin.String,
        trackingUrl: kotlin.String
    ): com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO {
        return com.rj.ecommerce.api.shared.messaging.email.OrderEmailRequestDTO.builder()
            .messageId(java.util.UUID.randomUUID().toString())
            .version("1.0")
            .to(order.user!!.getEmail().value)
            .template(com.rj.ecommerce.api.shared.enums.EmailTemplate.ORDER_SHIPMENT)
            .orderId(order.id.toString())
            .orderNumber(order.id.toString())
            .customer(mapCustomer(order.user!!))
            .items(mapOrderItems(order))
            .totalAmount(mapMoney(order.totalAmount, order.currency.name))
            .shippingAddress(mapAddress(order.shippingAddress))
            .shippingMethod(order.shippingMethod)
            .orderDate(order.createdAt)
            .orderStatus(order.orderStatus)
            .additionalData(
                java.util.Map.of<K?, V?>(
                    "trackingNumber", trackingNumber,
                    "trackingUrl", trackingUrl
                )
            )
            .build()
    }

    // Mapping methods
    private fun mapCustomer(user: com.rj.ecommerce_backend.user.domain.User): CustomerInfoDTO {
        return CustomerInfoDTO.builder()
            .id(user.getId().toString())
            .firstName(user.getFirstName())
            .lastName(user.getLastName())
            .email(user.getEmail().value)
            .phoneNumber(user.getPhoneNumber().value)
            .build()
    }

    private fun mapOrderItems(order: com.rj.ecommerce_backend.order.domain.Order): kotlin.collections.MutableList<OrderItemDTO?> {
        return order.orderItems.stream()
            .map<kotlin.Any?> { item: OrderItem? ->
                OrderItemDTO.builder()
                    .id(item.id.toString())
                    .productId(item.product.id.toString())
                    .productName(item.product.productName.value)
                    .quantity(item.quantity)
                    .unitPrice(mapMoney(item.price, order.currency.name))
                    .totalPrice(
                        mapMoney(
                            item.price.multiply(BigDecimal.valueOf(item.quantity.toLong())),
                            order.currency.name
                        )
                    )
                    .build()
            }
            .toList()
    }

    private fun mapMoney(amount: BigDecimal?, currencyCode: kotlin.String?): MoneyDTO {
        return MoneyDTO.builder()
            .amount(amount)
            .currencyCode(currencyCode)
            .build()
    }

    private fun mapAddress(address: com.rj.ecommerce_backend.user.valueobject.Address): AddressDTO {
        return AddressDTO.builder()
            .street(address.street)
            .city(address.city)
            .zipCode(address.zipCode.value)
            .country(address.country)
            .build()
    }
}
