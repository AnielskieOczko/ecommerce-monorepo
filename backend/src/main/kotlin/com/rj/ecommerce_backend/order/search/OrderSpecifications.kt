package com.rj.ecommerce_backend.order.search

import com.rj.ecommerce_backend.api.shared.core.Email
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.api.shared.enums.PaymentMethod
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import jakarta.persistence.criteria.Path
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.time.LocalDateTime

private val logger = KotlinLogging.logger { }

private const val PAYMENT_TRANSACTION_ID_FIELD = "paymentTransactionId"
private const val USER_FIELD = "user"
private const val ID_FIELD = "id"
private const val EMAIL_FIELD = "email"
private const val EMAIL_VALUE_FIELD = "value" // For Email.value
private const val FIRST_NAME_FIELD = "firstName"
private const val LAST_NAME_FIELD = "lastName"
private const val ORDER_STATUS_FIELD = "orderStatus"
private const val TOTAL_AMOUNT_FIELD = "totalAmount"
private const val ORDER_DATE_FIELD = "orderDate"
private const val PAYMENT_METHOD_FIELD = "paymentMethod"

object OrderSpecifications {


    fun withSearchCriteria(search: String?): Specification<Order>? {

        if (search.isNullOrBlank()) return null

        return Specification { root, _, cb ->
            logger.debug { "Applying order search criteria: '$search'" }
            val searchLower = "%${search.lowercase()}%"

            val predicates = mutableListOf(
                cb.like(cb.lower(root.get(PAYMENT_TRANSACTION_ID_FIELD)), searchLower)
            )

            val joinUser: Join<Order, User> = root.join(USER_FIELD, JoinType.INNER)
            predicates.add(cb.like(cb.lower(joinUser.get<Email>(EMAIL_FIELD)[EMAIL_VALUE_FIELD]), searchLower))
            predicates.add(cb.like(cb.lower(joinUser.get(FIRST_NAME_FIELD)), searchLower))
            predicates.add(cb.like(cb.lower(joinUser.get(LAST_NAME_FIELD)), searchLower))

            try {
                val searchId: Long = search.toLong()
                predicates.add(cb.equal(root.get<Long>(ID_FIELD), searchId))
            } catch (e: NumberFormatException) {
                logger.debug { "Search term '$search' is not a numeric Order ID. Searching text fields." }
            }
            cb.or(*predicates.toTypedArray()) // Spread the list of predicates into varargs for cb.or()
        }

    }

    fun withStatus(status: OrderStatus?): Specification<Order>? {
        logger.debug { "Filtering orders by status: $status" }
        if (status == null) return null

        return Specification { root, _, cb ->
            cb.equal(root.get<OrderStatus>(ORDER_STATUS_FIELD), status)
        }
    }

    fun withTotalAmountRange(minTotalAmount: BigDecimal?, maxTotalAmount: BigDecimal?): Specification<Order>? {
        if (minTotalAmount == null && maxTotalAmount == null) return null // Check both null first
        return Specification { root, _, cb ->
            logger.debug { "Filtering orders by total amount range: min=${minTotalAmount ?: "N/A"}, max=${maxTotalAmount ?: "N/A"}" }
            val totalAmountPath: Path<BigDecimal> = root.get(TOTAL_AMOUNT_FIELD)

            when {
                minTotalAmount != null && maxTotalAmount != null -> cb.between(
                    totalAmountPath,
                    minTotalAmount,
                    maxTotalAmount
                )

                minTotalAmount != null -> cb.greaterThanOrEqualTo(totalAmountPath, minTotalAmount)
                maxTotalAmount != null -> cb.lessThanOrEqualTo(totalAmountPath, maxTotalAmount)
            }

            cb.lessThanOrEqualTo(totalAmountPath, maxTotalAmount)
        }
    }

    fun createdBetween(start: LocalDateTime?, end: LocalDateTime?): Specification<Order>? {
        if (start == null && end == null) return null

        return Specification { root, _, cb ->
            logger.debug { "Filtering orders created between ${start ?: "N/A"} and ${end ?: "N/A"}" }
            val orderDatePath: Path<LocalDateTime> = root.get(ORDER_DATE_FIELD)

            when {
                start != null && end != null -> cb.between(orderDatePath, start, end)
                start != null -> cb.greaterThanOrEqualTo(orderDatePath, start)
                end != null -> cb.lessThanOrEqualTo(orderDatePath, end)

                else -> null
            }
        }
    }

    fun withUserId(userId: Long?): Specification<Order>? {
        if (userId == null) return null
        logger.debug { "Filtering orders by user ID: $userId" }

        return Specification { root, _, cb ->

            val userIdPath: Path<Long> = root.get<User>(USER_FIELD)[ID_FIELD]
            cb.equal(userIdPath, userId)
        }
    }

    fun withPaymentMethod(paymentMethod: PaymentMethod?): Specification<Order>? {
        if (paymentMethod == null) return null
        logger.debug { "Filtering orders by payment method: $paymentMethod" }
        return Specification { root, _, cb ->
            cb.equal(root.get<PaymentMethod>(PAYMENT_METHOD_FIELD), paymentMethod)
        }
    }

    fun hasTransactionId(hasTransactionId: Boolean?): Specification<Order>? {
        if (hasTransactionId == null) return null
        logger.debug { "Filtering orders by transaction ID presence: $hasTransactionId" }
        return Specification { root, _, cb ->
            val transactionIdPath: Path<String> = root[PAYMENT_TRANSACTION_ID_FIELD]

            if (hasTransactionId) {
                cb.isNotNull(transactionIdPath)
            } else {
                cb.isNull(transactionIdPath)
            }
        }
    }
}