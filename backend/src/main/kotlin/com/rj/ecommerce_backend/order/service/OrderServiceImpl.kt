package com.rj.ecommerce_backend.order.service

import com.rj.ecommerce.api.shared.dto.cart.CartDTO
import com.rj.ecommerce.api.shared.dto.cart.CartItemDTO
import com.rj.ecommerce_backend.messaging.common.excepion.MessagePublishException
import com.rj.ecommerce_backend.messaging.email.EmailRequestFactory
import com.rj.ecommerce_backend.messaging.email.EmailServiceClient
import com.rj.ecommerce_backend.messaging.email.contract.v1.EcommerceEmailRequest
import com.rj.ecommerce_backend.messaging.payment.dto.CheckoutSessionResponseDTO
import com.rj.ecommerce_backend.order.domain.Order
import com.rj.ecommerce_backend.order.domain.OrderItem
import com.rj.ecommerce_backend.order.domain.OrderItem.price
import com.rj.ecommerce_backend.order.domain.OrderItem.quantity
import com.rj.ecommerce_backend.order.dtos.OrderCreationRequest
import com.rj.ecommerce_backend.order.dtos.OrderDTO
import com.rj.ecommerce_backend.order.dtos.ShippingAddressDTO
import com.rj.ecommerce_backend.order.enums.*
import com.rj.ecommerce_backend.order.enums.Currency
import com.rj.ecommerce_backend.order.exceptions.OrderCancellationException
import com.rj.ecommerce_backend.order.exceptions.OrderNotFoundException
import com.rj.ecommerce_backend.order.exceptions.OrderServiceException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.repository.OrderRepository
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exceptions.InsufficientStockException
import com.rj.ecommerce_backend.product.exceptions.ProductNotFoundException
import com.rj.ecommerce_backend.product.service.ProductService
import com.rj.ecommerce_backend.securityconfig.SecurityContextImpl
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.exceptions.UserNotFoundException
import com.rj.ecommerce_backend.user.services.AdminService
import com.rj.ecommerce_backend.user.valueobject.Address
import com.rj.ecommerce_backend.user.valueobject.ZipCode
import jakarta.transaction.Transactional
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.*
import java.util.function.Function
import java.util.function.Supplier

@RequiredArgsConstructor
@Service
@Slf4j
class OrderServiceImpl : OrderService {
    private val orderRepository: OrderRepository? = null
    private val securityContext: SecurityContextImpl? = null
    private val productService: ProductService? = null
    private val adminService: AdminService? = null
    private val orderMapper: OrderMapper? = null
    private val emailServiceclient: EmailServiceClient? = null
    private val emailRequestFactory: EmailRequestFactory? = null


    @Transactional
    override fun createOrder(userId: Long?, orderCreationRequest: OrderCreationRequest): OrderDTO? {
        try {
            // First create and save the order to get an ID
            val order = createInitialOrder(userId, orderCreationRequest)

            // Try to send notification, but don't let email failure prevent order creation
            try {
                val request: EcommerceEmailRequest = emailRequestFactory!!.createOrderConfirmationRequest(order)
                emailServiceclient!!.sendEmailRequest(request)
            } catch (e: MessagePublishException) {
                // Log the email failure but don't roll back the transaction
                OrderServiceImpl.log.error(
                    "Failed to send order confirmation email for order ID: {}. Order was created successfully.",
                    order.id, e
                )
            }

            return orderMapper!!.toDto(order)
        } catch (e: Exception) {
            OrderServiceImpl.log.error("Error creating order for user {}", userId, e)
            throw OrderServiceException("Error creating order", e)
        }
    }

    @Transactional
    override fun getOrderByIdAdmin(orderId: Long?): Optional<Order?> {
        securityContext!!.isAdmin()

        if (orderId == null) {
            OrderServiceImpl.log.warn("Attempted to retrieve order with null ID")
            return Optional.empty<Order?>()
        }

        // Use the repository method that eagerly fetches order items
        val orderOptional = orderRepository!!.findById(orderId)

        // Handle case where order doesn't exist
        if (orderOptional.isEmpty()) {
            OrderServiceImpl.log.info("Order not found with ID: {}", orderId)
            return Optional.empty<Order?>()
        }

        val order = orderOptional.get()

        // Check user access permissions
        try {
            val orderUser = order.user
            securityContext.checkAccess(orderUser!!.getId())

            OrderServiceImpl.log.info("Successfully retrieved order with ID: {}", orderId)
            return Optional.of<Order?>(order)
        } catch (e: AccessDeniedException) {
            OrderServiceImpl.log.warn("Access denied for order ID: {} for user", orderId)
            throw e
        } catch (e: Exception) {
            OrderServiceImpl.log.error("Unexpected error retrieving order with ID: {}", orderId, e)
            throw OrderServiceException("Error processing order retrieval", e)
        }
    }

    @Transactional
    override fun getOrderById(userId: Long?, orderId: Long?): Optional<Order?> {
        securityContext!!.checkAccess(userId)

        if (orderId == null) {
            OrderServiceImpl.log.warn("Attempted to retrieve order with null ID")
            return Optional.empty<Order?>()
        }

        val orderOptional = orderRepository!!.findById(orderId)

        // Handle case where order doesn't exist
        if (orderOptional.isEmpty()) {
            OrderServiceImpl.log.info("Order not found with ID: {}", orderId)
            return Optional.empty<Order?>()
        }

        val order = orderOptional.get()

        return Optional.of<Order?>(order)
    }

    @Transactional
    override fun getOrderByIdWithOrderItems(orderId: Long?): Optional<Order?> {
        val userId = securityContext!!.getCurrentUser().getId()
        securityContext.checkAccess(userId)

        if (orderId == null) {
            OrderServiceImpl.log.warn("Attempted to retrieve order with null ID")
            return Optional.empty<Order?>()
        }

        val orderOptional = orderRepository!!.findByIdWithOrderItems(orderId, userId)

        // Handle case where order doesn't exist
        if (orderOptional.isEmpty()) {
            OrderServiceImpl.log.info("Order not found with ID: {}", orderId)
            return Optional.empty<Order?>()
        }

        val order = orderOptional.get()

        return Optional.of<Order?>(order)
    }

    @Transactional
    override fun getOrdersForUser(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO?> {
        val spec = criteria.toSpecification()

        securityContext!!.checkAccess(criteria.userId)
        OrderServiceImpl.log.debug("Fetching orders for user ID: {}", criteria.userId)
        return orderRepository!!.findAll(spec, pageable)
            .map<U?>(Function { order: Order? -> orderMapper!!.toDto(order) })
    }

    @Transactional
    override fun getAllOrders(pageable: Pageable, criteria: OrderSearchCriteria): Page<OrderDTO?> {
        // Verify admin permissions
        if (securityContext!!.isAdmin()) {
            OrderServiceImpl.log.warn("Unauthorized access attempt to all orders")
            throw AccessDeniedException("Admin access required")
        }

        val orderSpecification = criteria.toSpecification()

        val orders = orderRepository!!.findAll(orderSpecification, pageable)

        // Convert to DTOs
        return orders.map<U?>(Function { order: Order? -> orderMapper!!.toDto(order) })
    }

    @Transactional
    override fun updateOrderStatus(orderId: Long, newStatus: OrderStatus?): OrderDTO? {
        val order = orderRepository!!.findById(orderId)
            .orElseThrow<OrderNotFoundException?>(Supplier { OrderNotFoundException(orderId) })

        order.orderStatus = newStatus
        val updatedOrder = orderRepository.save<Order>(order)

        OrderServiceImpl.log.info("Order {} status updated to {}", orderId, newStatus)
        return orderMapper!!.toDto(updatedOrder)
    }

    @Transactional
    override fun cancelOrder(userId: Long?, orderId: Long) {
        val order = orderRepository!!.findById(orderId)
            .orElseThrow<OrderNotFoundException?>(Supplier { OrderNotFoundException(orderId) })

        // Validate order belongs to user
        if (order.user!!.getId() != userId) {
            throw AccessDeniedException("User " + userId + " is not authorized to cancel order " + orderId)
        }

        // Validate order status
        if (order.orderStatus != OrderStatus.PENDING) {
            throw OrderCancellationException("Cannot cancel order with status: " + order.orderStatus)
        }

        order.orderStatus = OrderStatus.CANCELLED
        orderRepository.save<Order?>(order)

        OrderServiceImpl.log.info("Order {} cancelled by user {}", orderId, userId)
    }

    @Transactional
    override fun cancelOrderAdmin(orderId: Long) {
        val order = orderRepository!!.findById(orderId)
            .orElseThrow<OrderNotFoundException?>(Supplier { OrderNotFoundException(orderId) })

        order.orderStatus = OrderStatus.CANCELLED
        orderRepository.save<Order?>(order)

        OrderServiceImpl.log.info("Order {} cancelled by admin", orderId)
    }

    @Transactional
    override fun calculateOrderTotal(cartItems: MutableList<CartItemDTO?>): BigDecimal? {
        return cartItems.stream()
            .map<Any?> { item: CartItemDTO? -> item.price().multiply(BigDecimal.valueOf(item.quantity())) }
            .reduce(BigDecimal.ZERO, BigDecimal::add)
    }

    fun markOrderPaymentFailed(orderId: Long) {
        val order = orderRepository!!.findById(orderId)
            .orElseThrow<OrderNotFoundException?>(Supplier { OrderNotFoundException(orderId) })

        order.paymentStatus = PaymentStatus.FAILED
        order.orderStatus = OrderStatus.FAILED
        orderRepository.save<Order?>(order)
    }

    @Transactional
    override fun updateOrderWithCheckoutSession(response: CheckoutSessionResponseDTO) {
        val orderId = response.orderId.toLong()
        val paymentStatus = response.paymentStatus

        // Use the repository method that eagerly fetches order items
        val order = orderRepository!!.findById(orderId)
            .orElseThrow<OrderNotFoundException?>(Supplier { OrderNotFoundException(orderId) })

        // Update payment status
        order.paymentStatus = paymentStatus

        // Update session ID if available
        if (response.sessionId != null) {
            order.paymentTransactionId = response.sessionId
        }

        // Update checkout URL if available
        if (response.checkoutUrl != null) {
            order.checkoutSessionUrl = response.checkoutUrl
        }

        if (response.expiresAt != null) {
            order.checkoutSessionExpiresAt = response.expiresAt
        }

        // Store receipt URL if available in additional details
        if (response.additionalDetails != null && response.additionalDetails.containsKey("receiptUrl")) {
            order.receiptUrl = response.additionalDetails.get("receiptUrl")
        }

        // Update order status based on payment status
        if (paymentStatus == PaymentStatus.SUCCEEDED || paymentStatus == PaymentStatus.PAID) {
            order.orderStatus = OrderStatus.CONFIRMED
        } else if (paymentStatus == PaymentStatus.FAILED) {
            order.orderStatus = OrderStatus.FAILED
        }

        order.updatedAt = LocalDateTime.now()
        orderRepository.save<Order?>(order)

        // Log with available information
        if (response.sessionId != null) {
            OrderServiceImpl.log.info(
                "Updated order {} with payment status {}, session ID: {}",
                orderId, paymentStatus, response.sessionId
            )
        } else {
            OrderServiceImpl.log.info("Updated order {} with payment status {}", orderId, paymentStatus)
        }

        // Log receipt URL if available
        if (response.additionalDetails != null && response.additionalDetails.containsKey("receiptUrl")) {
            OrderServiceImpl.log.info(
                "Receipt URL for order {}: {}",
                orderId,
                response.additionalDetails.get("receiptUrl")
            )
        }
    }

    override fun updatePaymentDetailsOnInitiation(order: Order) {
        // Update payment fields
        order.paymentStatus = PaymentStatus.PENDING
        order.updatedAt = LocalDateTime.now()
        orderRepository!!.save<Order?>(order)
    }

    @Transactional
    private fun createInitialOrder(userId: Long?, request: OrderCreationRequest): Order {
        // Validate user access and existence
        securityContext!!.checkAccess(userId)
        val user = adminService!!.getUserForValidation(userId)
            .orElseThrow<UserNotFoundException?>(Supplier { UserNotFoundException("User not found") })

        // Validate product stock before processing
        validateCartAvailability(request.cart)

        // Create order entity
        val order = createOrderEntity(
            user,
            request.shippingAddress,
            request.shippingMethod,
            request.paymentMethod,
            request.cart
        )

        // Set initial status
        order.paymentStatus = PaymentStatus.PENDING
        order.orderStatus = OrderStatus.PENDING
        order.orderDate = LocalDateTime.now()
        order.updatedAt = LocalDateTime.now()
        order.lastModifiedBy = user.getEmail().value

        // Create order items
        val orderItems = createOrderItems(order, request.cart)
        order.setOrderItems(orderItems)

        // Save and return
        return orderRepository!!.save<Order>(order)
    }

    private fun createOrderEntity(
        user: User,
        shippingAddress: ShippingAddressDTO,
        shippingMethod: ShippingMethod?,
        paymentMethod: PaymentMethod?,
        cartDTO: CartDTO
    ): Order {
        val order = Order()
        order.user = user
        order.createdBy = user.getEmail().value


        // Create Address from DTO
        val deliveryAddress = Address(
            shippingAddress.street,
            shippingAddress.city,
            ZipCode(shippingAddress.zipCode),
            shippingAddress.country
        )

        order.shippingAddress = deliveryAddress
        order.shippingMethod = shippingMethod
        order.paymentMethod = paymentMethod

        // Calculate total price
        val totalPrice = calculateOrderTotal(cartDTO.cartItems())
        order.currency = Currency.PLN
        order.totalAmount = totalPrice

        return order
    }

    private fun validateCartAvailability(cartDTO: CartDTO) {
        for (item in cartDTO.cartItems()) {
            val product = productService!!.getProductEntityForValidation(item.productId())
                .orElseThrow<ProductNotFoundException?>(Supplier { ProductNotFoundException(item.productId()) })

            if (product.getStockQuantity().value < item.quantity()) {
                throw InsufficientStockException("Insufficient stock for product: " + product.getProductName())
            }
        }
    }

    private fun createOrderItems(order: Order?, cartDTO: CartDTO): MutableList<OrderItem?> {
        return cartDTO.cartItems().stream()
            .map({ cartItemDTO ->
                val product = productService!!.getProductEntityForValidation(cartItemDTO.productId())
                    .orElseThrow<ProductNotFoundException?>(Supplier { ProductNotFoundException(cartItemDTO.productId()) })
                // Reduce inventory atomically
                productService.reduceProductQuantity(
                    product.getId(),
                    cartItemDTO.quantity()
                )
                createOrderItem(order, product, cartItemDTO)
            })
            .toList()
    }

    private fun createOrderItem(order: Order?, product: Product, cartItemDTO: CartItemDTO): OrderItem {
        val orderItem = OrderItem()
        orderItem.order = order
        orderItem.product = product
        orderItem.quantity = cartItemDTO.quantity()
        orderItem.price = product.getProductPrice().amount.value
        return orderItem
    }
}
