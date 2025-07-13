package com.rj.ecommerce_backend.order.usecases

import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.order.OrderCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.enums.*
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.events.order.OrderCreatedEvent
import com.rj.ecommerce_backend.order.exception.OrderServiceException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.service.ProductCommandServiceImpl
import com.rj.ecommerce_backend.product.service.ProductQueryService

import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

@Component
class CreateOrderUseCase(
    private val orderRepository: OrderRepository,
    private val productCommandServiceImpl: ProductCommandServiceImpl,
    private val productQueryService: ProductQueryService,
    private val securityContext: SecurityContext,
    private val orderMapper: OrderMapper,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun execute(userId: Long, request: OrderCreateRequestDTO): OrderDTO {
        try {
            val order = createInitialOrder(userId, request)

            // PUBLISH EVENT: Announce that an order was created.
            // The use case doesn't know or care what happens next.
            eventPublisher.publishEvent(OrderCreatedEvent(this, order))

            return orderMapper.toDto(order)
        } catch (e: Exception) {
            logger.error(e) { "Error creating order for user $userId" }
            throw OrderServiceException("Error creating order for user $userId", e)
        }
    }

    private fun createInitialOrder(userId: Long, request: OrderCreateRequestDTO): Order {
        securityContext.ensureAccess(userId)
        val user: User = securityContext.getCurrentUser()
        validateCartAvailability(request.cart)

        val newOrder = Order(
            user = user,
            shippingAddress = request.shippingAddress,
            shippingMethod = request.shippingMethod,
            paymentMethod = request.paymentMethod,
            currency = Currency.PLN,
            paymentStatus = CanonicalPaymentStatus.PENDING,
            orderStatus = OrderStatus.PENDING
        )

        val orderItems = createOrderItemsAndReduceStock(newOrder, request.cart)
        orderItems.forEach { newOrder.addOrderItem(it) }
        newOrder.totalAmount = calculateOrderTotal(request.cart.items.map { it.product.unitPrice?.amount ?: BigDecimal.ZERO }, request.cart.items.map { it.quantity })

        return orderRepository.save(newOrder)
    }

    private fun validateCartAvailability(cartDTO: CartDTO) {
        cartDTO.items.forEach { item ->
            val product = productQueryService.getProductEntityForValidation(item.product.id)
                ?: throw ProductNotFoundException(item.product.id)
            if ((product.quantityInStock.value) < item.quantity) {
                throw InsufficientStockException("Insufficient stock for product: ${product.name.value}")
            }
        }
    }

    private fun createOrderItemsAndReduceStock(order: Order, cartDTO: CartDTO): List<OrderItem> {
        return cartDTO.items.map { cartItemDTO ->
            val product = productQueryService.getProductEntityForValidation(cartItemDTO.product.id)
                ?: throw ProductNotFoundException(cartItemDTO.product.id)

            productCommandServiceImpl.reduceProductQuantity(product.id!!, cartItemDTO.quantity)

            OrderItem(
                order = order,
                product = product,
                quantity = cartItemDTO.quantity,
                price = product.unitPrice.amount
            )
        }
    }

    private fun calculateOrderTotal(prices: List<BigDecimal>, quantities: List<Int>): BigDecimal {
        return prices.zip(quantities) { price, quantity -> price.multiply(BigDecimal.valueOf(quantity.toLong())) }
            .sumOf { it }
    }

}