package com.rj.ecommerce_backend.order.usecases

import com.rj.ecommerce_backend.api.shared.dto.order.request.OrderCreateRequest
import com.rj.ecommerce_backend.api.shared.dto.order.request.OrderItemCreateRequest
import com.rj.ecommerce_backend.api.shared.dto.order.response.OrderResponse
import com.rj.ecommerce_backend.api.shared.enums.CanonicalPaymentStatus
import com.rj.ecommerce_backend.api.shared.enums.Currency
import com.rj.ecommerce_backend.api.shared.enums.OrderStatus
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.events.order.OrderCreatedEvent
import com.rj.ecommerce_backend.order.exception.OrderServiceException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.service.ProductCommandService
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
    private val productCommandService: ProductCommandService,
    private val productQueryService: ProductQueryService,
    private val securityContext: SecurityContext,
    private val orderMapper: OrderMapper,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun execute(userId: Long, request: OrderCreateRequest): OrderResponse {
        try {
            val order = createInitialOrder(userId, request)
            eventPublisher.publishEvent(OrderCreatedEvent(this, order))
            return orderMapper.toDto(order)
        } catch (e: Exception) {
            logger.error(e) { "Error creating order for user $userId" }
            throw OrderServiceException("Error creating order for user $userId", e)
        }
    }

    private fun createInitialOrder(userId: Long, request: OrderCreateRequest): Order {
        securityContext.ensureAccess(userId)
        val user: User = securityContext.getCurrentUser()


        // 1. Get unique product IDs from the request
        val productIds = request.items.map { it.productId }.toSet()

        // 2. Fetch all products at once
        val productsById = productQueryService.findProductEntitiesByIds(productIds.toList())
            .associateBy { it.id!! }

        // 3. Pre-join the requested items with the fetched products.
        val validatedOrderItemsData = request.items.map { itemRequest ->
            val product = productsById[itemRequest.productId]
                ?: throw ProductNotFoundException(itemRequest.productId) // This is a business error: client requested a product that doesn't exist.

            // Return a Pair containing the original request and the trusted Product entity
            itemRequest to product
        }


        // 4. Create order
        val newOrder = Order(
            user = user,
            shippingAddress = request.shippingAddress,
            shippingMethod = request.shippingMethod,
            paymentMethod = request.paymentMethod,
            currency = Currency.PLN, // Assuming a default currency
            paymentStatus = CanonicalPaymentStatus.PENDING,
            orderStatus = OrderStatus.PENDING
        )

        // 5. Pass the safe, pre-joined data to the next method.
        val orderItems = createOrderItemsAndReduceStock(newOrder, validatedOrderItemsData)

        // 6. Calculate total based on trusted server-side prices
        newOrder.totalAmount = orderItems.sumOf {
            it.price.multiply(BigDecimal.valueOf(it.quantity.toLong()))
        }

        return orderRepository.save(newOrder)
    }

    private fun createOrderItemsAndReduceStock(
        order: Order,
        validatedItemsData: List<Pair<OrderItemCreateRequest, Product>>
    ): List<OrderItem> {
        return validatedItemsData.map { (itemRequest, product) ->

            // Validate stock and reduce it
            if (product.quantityInStock.value < itemRequest.quantity) {
                throw InsufficientStockException("Insufficient stock for product: ${product.name}")
            }
            productCommandService.reduceProductQuantity(product.id!!, itemRequest.quantity)

            // Create the OrderItem using the trusted server-side price
            OrderItem(
                order = order,
                product = product,
                quantity = itemRequest.quantity,
                price = product.unitPrice.amount
            )
        }
    }
}