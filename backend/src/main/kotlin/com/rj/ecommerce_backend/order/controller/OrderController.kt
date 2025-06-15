package com.rj.ecommerce_backend.order.controller

import com.rj.ecommerce.api.shared.dto.order.OrderCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.order.exceptions.OrderNotFoundException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.order.service.OrderService
import com.rj.ecommerce_backend.sorting.OrderSortField
import com.rj.ecommerce_backend.sorting.SortValidator
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@Tag(name = "Order", description = "APIs for customer order management")
@RequestMapping("/api/v1")
class OrderController(
    private val orderService: OrderService,
    private val orderMapper: OrderMapper,
    private val sortValidator: SortValidator
) {
    companion object {
        val logger = KotlinLogging.logger { }
    }

    @GetMapping("/users/{userId}/orders")
    @Operation(summary = "Get all orders for a user", description = "Retrieves a paginated and filtered list of orders for a specific user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orders")
    @ApiResponse(responseCode = "400", description = "Invalid sorting or filtering parameters")
    @ApiResponse(responseCode = "403", description = "Forbidden access")
    @PreAuthorize("#pathUserId == authentication.principal.id or hasRole('ADMIN')")
    fun getAllOrdersForUser(
        @PathVariable pathUserId: Long,

        criteriaInput: OrderSearchCriteria,

        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<OrderDTO>> {

        logger.info {
            "Request to get orders for user ID: $pathUserId with criteria: $criteriaInput, " +
                    "page=$page, size=$size, sort=$sort"
        }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort,
            OrderSortField::class.java
        )
        val pageable = PageRequest.of(page, size, validatedSort)

        val serviceCriteria = OrderSearchCriteria(
            search = criteriaInput.search,
            status = criteriaInput.status,
            minAmount = criteriaInput.minAmount,
            maxAmount = criteriaInput.maxAmount,
            startDate = criteriaInput.startDate,
            endDate = criteriaInput.endDate,
            userId = pathUserId, // IMPORTANT: Always scope by the pathUserId for this endpoint
            paymentMethod = criteriaInput.paymentMethod,
            hasTransactionId = criteriaInput.hasTransactionId
        )

        val ordersPage = orderService.getOrdersForUser(pageable, serviceCriteria)
        logger.info { "Retrieved ${ordersPage.numberOfElements} orders for user ID $pathUserId on page $page." }
        return ResponseEntity.ok(ordersPage)
    }

    @GetMapping("/users/{userId}/orders/{orderId}")
    @Operation(summary = "Get a specific order", description = "Retrieves a single order by its ID for a specific user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the order")
    @ApiResponse(responseCode = "404", description = "Order not found")
    @ApiResponse(responseCode = "403", description = "Forbidden access")
    fun getOrder(
        @PathVariable userId: Long,
        @PathVariable orderId: Long
    ): ResponseEntity<OrderDTO> {
        logger.info { "Request to get order ID: $orderId for user ID: $userId" }

        val order = orderService.getOrderById(userId, orderId)
            ?: throw OrderNotFoundException(orderId)

        val orderDto = orderMapper.toDto(order)

        return ResponseEntity.ok(orderDto)

    }

    @PostMapping("/users/{userId}/orders")
    @Operation(summary = "Create a new order", description = "Creates a new order from the user's cart and shipping details.")
    @ApiResponse(responseCode = "201", description = "Order created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid order data, e.g., insufficient stock")
    @ApiResponse(responseCode = "403", description = "Forbidden access")
    @PreAuthorize("#userId == authentication.principal.id")
    fun createOrder(
        @PathVariable userId: Long,
        @Valid @RequestBody orderCreateRequestDTO: OrderCreateRequestDTO
    ): ResponseEntity<OrderDTO> {
        logger.info { "Request to create order for user ID: $userId" }
        val orderDto = orderService.createOrder(userId, orderCreateRequestDTO)
        logger.info { "Successfully created order ID: ${orderDto.id} for user ID: $userId" }
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto)
    }

    @DeleteMapping("/users/{userId}/orders/{orderId}")
    @Operation(summary = "Cancel an order", description = "Allows a user or an admin to cancel an order.")
    @ApiResponse(responseCode = "204", description = "Order cancelled successfully")
    @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in its current state")
    @PreAuthorize("#userId == authentication.principal.id or hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelOrder(
        @PathVariable userId: Long,
        @PathVariable orderId: Long
    ) {
        logger.info { "Request to cancel order ID: $orderId for user ID: $userId" }
        orderService.cancelOrder(userId, orderId)
        logger.info { "Successfully processed cancellation for order ID: $orderId" }
    }


}