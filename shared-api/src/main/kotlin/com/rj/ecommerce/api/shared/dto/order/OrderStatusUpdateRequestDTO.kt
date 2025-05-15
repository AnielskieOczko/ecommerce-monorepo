package com.rj.ecommerce.api.shared.dto.order

import com.rj.ecommerce.api.shared.enums.OrderStatus

/**
 * Request to update the status of an order.
 *
 * @property newStatus The new status to set for the order.
 *
 * Requirements:
 * - newStatus is required
 */
data class OrderStatusUpdateRequestDTO(
    val newStatus: OrderStatus
)
