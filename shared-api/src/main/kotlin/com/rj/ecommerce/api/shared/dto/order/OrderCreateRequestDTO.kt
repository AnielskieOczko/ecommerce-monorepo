package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.enums.PaymentMethod
import com.rj.ecommerce.api.shared.enums.ShippingMethod

/**
 * Request to create an order, typically from a cart.
 *
 * @property userId ID of the user creating the order.
 * @property shippingAddress Address where the order should be shipped.
 * @property billingAddress Address for billing (if different from shipping).
 * @property paymentMethod Method to be used for payment.
 * @property shippingMethod Method to be used for shipping.
 *
 * Requirements:
 * - userId, shippingAddress, paymentMethod, and shippingMethod are required
 * - billingAddress is optional
 */
data class OrderCreateRequest(
    val userId: Long,
    val shippingAddress: Address,
    val billingAddress: Address? = null,
    val paymentMethod: PaymentMethod,
    val shippingMethod: ShippingMethod
)
