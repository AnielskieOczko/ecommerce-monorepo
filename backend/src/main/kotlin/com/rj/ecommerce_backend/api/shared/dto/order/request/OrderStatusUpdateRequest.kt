package com.rj.ecommerce_backend.api.shared.dto.order.request

import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Request to update the status of an existing order.")
data class OrderStatusUpdateRequest(
    @field:Schema(description = "The new status to set for the order.", required = true)
    val newStatus: OrderStatus
)
