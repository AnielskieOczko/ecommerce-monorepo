package com.rj.ecommerce_backend.order.controller

import com.rj.ecommerce.api.shared.dto.order.OrderCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.order.service.OrderQueryService
import com.rj.ecommerce_backend.order.usecases.CreateOrderUseCase
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
import org.springframework.web.bind.annotation.*

@RestController
@Tag(name = "Order", description = "APIs for customer order management")
@RequestMapping("/api/v1")
class OrderController(
    private val orderQueryService: OrderQueryService,
    private val orderCommandService: OrderCommandService,
    private val createOrderUseCase: CreateOrderUseCase,
    private val sortValidator: SortValidator
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping("/users/{userId}/orders")
    @Operation(summary = "Get all orders for a user", description = "Retrieves a paginated and filtered list of orders for a specific user.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orders")
    @ApiResponse(responseCode = "400", description = "Invalid sorting or filtering parameters")
    @ApiResponse(responseCode = "403", description = "Forbidden access")
    @PreAuthorize("#pathUserId == authentication.principal.id or hasRole('ADMIN')")
    fun getAllOrdersForUser(
        @PathVariable("userId") pathUserId: Long,
        criteriaInput: OrderSearchCriteria,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id:asc") sort: String?
    ): ResponseEntity<Page<OrderDTO>> {
        logger.info { "Request received: Get orders for user ID: $pathUserId with criteria: $criteriaInput, page: $page, size: $size, sort: $sort" }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(sort, OrderSortField::class.java)
        val pageable = PageRequest.of(page, size, validatedSort)

        // The criteria DTO is copied to ensure the userId from the path is enforced,
        // preventing any potential manipulation from the request body.
        val serviceCriteria = criteriaInput.copy(userId = pathUserId)

        val ordersPage = orderQueryService.getOrdersForUser(pageable, serviceCriteria)

        logger.info { "Successfully retrieved ${ordersPage.numberOfElements} orders on page $page for user ID $pathUserId (Total: ${ordersPage.totalElements})." }
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
        logger.info { "Request received: Get order ID: $orderId for user ID: $userId" }

        val orderDto = orderQueryService.getOrderById(userId, orderId)
            ?: throw OrderNotFoundException(orderId)

        logger.info { "Successfully retrieved order ID: $orderId for user ID: $userId." }
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
        logger.info { "Request received: Create order for user ID: $userId" }

        val orderDto = createOrderUseCase.execute(userId, orderCreateRequestDTO)

        logger.info { "Successfully created order ID: ${orderDto.id} for user ID: $userId." }
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
        logger.info { "Request received: Cancel order ID: $orderId for user ID: $userId" }

        orderCommandService.cancelOrder(userId, orderId)

        logger.info { "Successfully processed cancellation for order ID: $orderId." }
    }
}