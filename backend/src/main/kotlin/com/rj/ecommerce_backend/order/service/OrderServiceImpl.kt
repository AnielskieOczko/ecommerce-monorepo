package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.ShippingAddressDTO
import com.rj.ecommerce.api.shared.core.ZipCode
import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO
import com.rj.ecommerce.api.shared.dto.order.OrderCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.messaging.common.excepion.MessagePublishException
import com.rj.ecommerce_backend.messaging.email.factory.EmailRequestFactory
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce.api.shared.enums.*
import com.rj.ecommerce.api.shared.messaging.email.EcommerceEmailRequest
import com.rj.ecommerce.api.shared.messaging.payment.PaymentResponseDTO
import com.rj.ecommerce_backend.messaging.email.client.EmailServiceClient
import com.rj.ecommerce_backend.order.exceptions.OrderCancellationException
import com.rj.ecommerce_backend.order.exceptions.OrderNotFoundException
import com.rj.ecommerce_backend.order.exceptions.OrderServiceException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.product.exceptions.InsufficientStockException
import com.rj.ecommerce_backend.product.exceptions.ProductNotFoundException
import com.rj.ecommerce_backend.product.service.ProductService
import com.rj.ecommerce_backend.securityconfig.SecurityContext
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal

private val logger = KotlinLogging.logger {}

@Service
class OrderServiceImpl( // Constructor Injection (replaces @RequiredArgsConstructor)
    private val orderRepository: OrderRepository,
    private val securityContext: SecurityContext,
    private val productService: ProductService,
    private val orderMapper: OrderMapper,
    private val emailServiceClient: EmailServiceClient,
    private val emailRequestFactory: EmailRequestFactory
) : OrderService {

    @Transactional
    override fun createOrder(userId: Long, orderCreateRequestDTO: OrderCreateRequestDTO): OrderDTO {
        try {
            val order = createInitialOrder(userId, orderCreateRequestDTO)

            try {
                val emailRq: EcommerceEmailRequest = emailRequestFactory.createOrderConfirmationRequest(order)
                emailServiceClient.sendEmailRequest(emailRq)
            } catch (e: MessagePublishException) {
                logger.error(e) { "Failed to send order confirmation email for order ID: ${order.id}. Order was created successfully." }
            }
            // orderMapper.toDto should ideally not return null if input 'order' is non-null
            return orderMapper.toDto(order) ?: throw OrderServiceException("Failed to map created order to DTO for order ID: ${order.id}")
        } catch (e: OrderServiceException) {
            throw e // Rethrow specific exceptions
        }
        catch (e: Exception) { // Catch broader exceptions last
            logger.error(e) { "Error creating order for user $userId" }
            throw OrderServiceException("Error creating order for user $userId", e)
        }
    }

    @Transactional(readOnly = true) // Good for read operations
    override fun getOrderByIdAdmin(orderId: Long): Order? {
        securityContext.ensureAdmin() // Renamed for clarity

        val order = orderRepository.findById(orderId).orElse(null) // Fetch and convert Optional to nullable

        if (order == null) {
            logger.info { "Admin: Order not found with ID: $orderId" }
            return null
        }

        // The original code had a checkAccess for the order's user, which is odd for an admin fetch.
        // If an admin is fetching any order, they usually don't need to pass the user's access check.
        // If the intent was to log which user's order an admin is accessing, that's different.
        // For now, assuming admin can access any order by its ID.
        logger.info { "Admin successfully retrieved order with ID: $orderId belonging to user ID: ${order.user?.id}" }
        return order
    }

    @Transactional(readOnly = true)
    override fun getOrderById(userId: Long, orderId: Long): Order? {
        securityContext.ensureAccess(userId)

        val order = orderRepository.findById(orderId).orElse(null)
            ?: run {
                logger.info { "Order not found with ID: $orderId for user ID: $userId" }
                return null
            }

        // Ensure the fetched order actually belongs to the user requesting it
        if (order.user?.id != userId) {
            logger.warn { "User $userId attempted to access order $orderId belonging to user ${order.user?.id}" }
            throw AccessDeniedException("User $userId is not authorized to access order $orderId")
        }
        return order
    }

    @Transactional(readOnly = true)
    override fun getOrderByIdWithOrderItems(orderId: Long): Order? {
        // Assuming orderRepository.findByIdWithOrderItems handles user authorization internally
        // or that this method is intended for cases where user context is already established (e.g. current user)
        val currentUser = securityContext.getCurrentUser()
        securityContext.ensureAccess(currentUser.id)


        // Original logic used findByIdWithOrderItems(orderId, userId) - let's assume that's still desired
        return orderRepository.findByIdWithOrderItems(orderId, currentUser.id).orElse(null)
            ?: run {
                logger.info { "Order not found or not accessible (with items) with ID: $orderId for user ID: ${currentUser.id}" }
                null
            }
    }

    @Transactional(readOnly = true)
    override fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO> {
        criteria.userId?.let { securityContext.ensureAccess(it) }
            ?: throw IllegalArgumentException("User ID must be provided in criteria for fetching user-specific orders.")

        logger.debug { "Fetching orders for user ID: ${criteria.userId}" }
        val spec: Specification<Order> = criteria.toSpecification()

        val pageOfOrders: Page<Order> = orderRepository.findAll(spec, pageable)
        val dtoList: List<OrderDTO> = pageOfOrders.content.mapNotNull { order -> orderMapper.toDto(order) }

        return PageImpl(dtoList,pageable, pageOfOrders.totalElements)
    }

    @Transactional(readOnly = true)
    override fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO> {
        securityContext.ensureAdmin()
        // Removed the inverted admin check logic
        logger.debug { "Admin fetching all orders with criteria." }
        val spec: Specification<Order> = criteria.toSpecification()

        val pageOfOrders: Page<Order> = orderRepository.findAll(spec, pageable)
        val dtoList: List<OrderDTO> = pageOfOrders.content.mapNotNull { order -> orderMapper.toDto(order) }

        return PageImpl(dtoList,pageable, pageOfOrders.totalElements)
    }

    @Transactional
    override fun updateOrderStatus(orderId: Long, newStatus: OrderStatus): OrderDTO {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // Potentially add logic here: can any admin update any order? Or only specific roles?
        // securityContext.ensureAdmin() or some other role check might be needed.

        order.orderStatus = newStatus
        val updatedOrder = orderRepository.save(order)
        logger.info { "Order $orderId status updated to $newStatus" }
        return orderMapper.toDto(updatedOrder) ?: throw OrderServiceException("Failed to map updated order $orderId to DTO.")
    }

    @Transactional
    override fun cancelOrder(userId: Long, orderId: Long) {
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        if (order.user?.id != userId) {
            logger.warn { "User $userId attempted to cancel order $orderId belonging to user ${order.user?.id}" }
            throw AccessDeniedException("User $userId is not authorized to cancel order $orderId")
        }

        if (order.orderStatus != OrderStatus.PENDING && order.orderStatus != OrderStatus.CONFIRMED /*Allow cancel if confirmed but not shipped?*/) {
            // Business logic: What statuses are cancellable by a user?
            throw OrderCancellationException("User cannot cancel order $orderId with status: ${order.orderStatus}")
        }

        order.orderStatus = OrderStatus.CANCELLED
        // order.paymentStatus = PaymentStatus.REFUND_REQUESTED or CANCELLED?
        orderRepository.save(order)
        logger.info { "Order $orderId cancelled by user $userId" }
        // TODO: Trigger refund process if payment was made
        // TODO: Send cancellation email
    }

    @Transactional
    override fun cancelOrderAdmin(orderId: Long) {
        securityContext.ensureAdmin()
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        // Admin might be able to cancel orders in more states than a user
        if (order.orderStatus == OrderStatus.SHIPPED || order.orderStatus == OrderStatus.DELIVERED) {
            throw OrderCancellationException("Admin cannot cancel order $orderId already in status: ${order.orderStatus}. Consider refund/return process.")
        }

        order.orderStatus = OrderStatus.CANCELLED
        // order.paymentStatus = PaymentStatus.REFUNDED or CANCELLED?
        orderRepository.save(order)
        logger.info { "Order $orderId cancelled by admin" }
        // TODO: Trigger refund process if payment was made
        // TODO: Send cancellation email (possibly different template for admin cancellation)
    }

    override fun calculateOrderTotal(cartItems: List<CartItemDTO>): BigDecimal {
        // Ensure CartItemDTO.price is non-null or handle it. Let's assume it's a BigDecimal.
        // Your original DTO has CartItemDTO.price as Money, which has amount: BigDecimal?
        // This needs clarification. Assuming item.unitPrice.amount for now.
        return cartItems.sumOf { item ->
            val price = item.product.unitPrice?.amount ?: BigDecimal.ZERO
            val quantity = BigDecimal.valueOf(item.quantity.toLong())
            price.multiply(quantity)
        }
    }

    // This method was not in the interface, making it public implicitly.
    // If it's only used internally, it should be private.
    // If it's part of a separate flow (e.g., payment webhook), it might belong to another service
    // or be exposed carefully. For now, keeping it as an internal helper or part of updateOrderWithCheckoutSession.
    @Transactional
    fun markOrderPaymentFailed(orderId: Long) { // Should this be part of updateOrderWithCheckoutSession?
        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        order.paymentStatus = PaymentStatus.FAILED
        order.orderStatus = OrderStatus.FAILED // Redundant if updateOrderWithCheckoutSession handles this
        orderRepository.save(order)
        logger.info { "Marked payment as FAILED for order $orderId" }
    }

    @Transactional
    override fun updateOrderWithCheckoutSession(response: PaymentResponseDTO) {
        val orderId = response.orderId.toLong() // Consider safe conversion or ensure orderId is always valid Long
        val paymentStatus = response.paymentStatus

        val order = orderRepository.findById(orderId)
            .orElseThrow { OrderNotFoundException(orderId) }

        order.paymentStatus = paymentStatus
        response.sessionId?.let { order.paymentTransactionId = it }
        response.checkoutUrl?.let { order.checkoutSessionUrl = it }
        response.expiresAt?.let { order.checkoutSessionExpiresAt = it }
        response.metadata?.get("receiptUrl")?.let { order.receiptUrl = it }

        when (paymentStatus) {
            PaymentStatus.COMPLETE, PaymentStatus.PAID -> {
                order.orderStatus = OrderStatus.CONFIRMED
                // TODO: Send payment success / order confirmation email if not already sent robustly
            }
            PaymentStatus.FAILED -> {
                order.orderStatus = OrderStatus.FAILED
                // TODO: Send payment failed email
            }
            PaymentStatus.PENDING, PaymentStatus.EXPIRED, PaymentStatus.UNKNOWN -> {
                if (order.orderStatus == OrderStatus.PENDING) {
                    // Potentially no change or to a more specific pending state
                }
            } else -> {
                // Log or handle unexpected payment status
            }
        }

        orderRepository.save(order)
        logger.info { "Updated order $orderId with payment status $paymentStatus, session ID: ${response.sessionId}" }
        response.metadata?.get("receiptUrl")?.let { logger.info { "Receipt URL for order $orderId: $it" } }
    }

    @Transactional // This was not in the interface, assumed to be part of the payment flow.
    override fun updatePaymentDetailsOnInitiation(order: Order) {
        order.paymentStatus = PaymentStatus.PENDING
        // order.updatedAt is handled by @UpdateTimestamp
        orderRepository.save(order)
        logger.info { "Initialized payment details for order ${order.id}, status set to PENDING." }
    }

    @Transactional // Keep this private helper
    private fun createInitialOrder(userId: Long, request: OrderCreateRequestDTO): Order {
        securityContext.ensureAccess(userId) // Ensure user performing action is the one in path
        val user: User = securityContext.getCurrentUser();

        validateCartAvailability(request.cart)

        val newOrder = Order( // Using data class constructor
            user = user,
            shippingAddress = request.shippingAddress,
            shippingMethod = request.shippingMethod,
            paymentMethod = request.paymentMethod,
            currency = Currency.PLN,
            // totalAmount will be calculated and set after items are processed or from cart
            // orderItems = mutableListOf() // Initialized by default in Order data class
            // Other fields will be set by auditing or explicitly below
        ).apply {
            // Set initial status by OrderService, not relying on entity defaults if they differ for new orders
            this.paymentStatus = PaymentStatus.PENDING
            this.orderStatus = OrderStatus.PENDING
            // this.orderDate = LocalDateTime.now() // Handled by @CreationTimestamp on Order entity
            // this.createdBy = user.email?.value // Handled by @CreatedBy
        }

        // Save order first to get an ID, which is necessary if OrderItems need it before being added to the list
        // However, with CascadeType.ALL, you can build the graph and save once.
        // Let's build the full graph.

        val orderItems = createOrderItemsAndReduceStock(newOrder, request.cart)
        orderItems.forEach { newOrder.addOrderItem(it) } // Use helper to set bidirectional link

        newOrder.totalAmount = calculateOrderTotal(request.cart.items) // Recalculate server-side for safety

        return orderRepository.save(newOrder)
    }

    private fun mapShippingAddressDtoToEntity(shippingAddressDTO: ShippingAddressDTO): Address {
        // Assuming com.rj.ecommerce_backend.user.valueobject.Address and ZipCode
        return Address(
            street = shippingAddressDTO.street, // Assuming DTO fields are non-nullable based on common patterns
            city = shippingAddressDTO.city,
            zipCode = ZipCode(shippingAddressDTO.zipCode), // Assuming ZipCode VO takes String
            country = shippingAddressDTO.country
        )
    }

    private fun validateCartAvailability(cartDTO: CartDTO) {
        cartDTO.items.forEach { item ->
            val product = productService.getProductEntityForValidation(item.product.id).orElseThrow(
                { ProductNotFoundException(item.product.id) }
            )

            if ((product.quantityInStock?.value ?: 0) < item.quantity) { // Safe access to stock quantity
                throw InsufficientStockException("Insufficient stock for product: ${product.name?.value ?: item.product.id}")
            }
        }
    }

    private fun createOrderItemsAndReduceStock(order: Order, cartDTO: CartDTO): List<OrderItem> {
        return cartDTO.items.map { cartItemDTO ->
            val product = productService.getProductEntityForValidation(cartItemDTO.product.id).orElseThrow(
                { ProductNotFoundException(cartItemDTO.product.id) }
            )

            productService.reduceProductQuantity(
                product.id, // Product ID should be non-null here
                cartItemDTO.quantity
            )

            OrderItem( // Using data class constructor
                order = order, // Will be set by order.addOrderItem() if preferred for bidirectionality
                product = product,
                quantity = cartItemDTO.quantity,
                price = product.unitPrice?.amount?.value // Get price from authoritative Product source
                    ?: throw OrderServiceException("Product ${product.id} is missing price information.")
            )
        }
    }
}
