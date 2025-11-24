package com.rj.ecommerce_backend.order.search

import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.order.domain.Order
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.LocalDateTime

data class OrderSearchCriteria(

    val search: String?,
    val status: OrderStatus?,
    val minAmount: BigDecimal?,
    val maxAmount: BigDecimal?,
    val startDate: LocalDateTime?,
    val endDate: LocalDateTime?,
    val userId: Long?,
    val paymentMethod: PaymentMethod?,
    val hasTransactionId: Boolean?
) {
    companion object {
        private val logger = KotlinLogging.logger {  }
    }

    fun toSpecification(): Specification<Order> {
        logger.debug {  }
        return Specification.unrestricted<Order>()
            .and(OrderSpecifications.withSearchCriteria(search))
            .and(OrderSpecifications.withStatus(status))
            .and(OrderSpecifications.withTotalAmountRange(minAmount, maxAmount))
            .and(OrderSpecifications.createdBetween(startDate, endDate))
            .and(OrderSpecifications.withUserId(userId))
            .and(OrderSpecifications.withPaymentMethod(paymentMethod))
            .and(OrderSpecifications.hasTransactionId(hasTransactionId))

    }

}