package com.rj.ecommerce_backend.order.repository

import com.rj.ecommerce_backend.order.domain.OrderItem
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemRepository: JpaRepository<OrderItem, Long> {

    /**
     * Efficiently checks if any OrderItem references a given product ID.
     * This is much faster than fetching all items.
     */
    fun existsByProductId(productId: Long): Boolean

}