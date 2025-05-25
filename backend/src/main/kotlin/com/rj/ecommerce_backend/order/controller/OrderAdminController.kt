package com.rj.ecommerce_backend.order.controller

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.dto.order.OrderStatusUpdateRequestDTO
import com.rj.ecommerce_backend.order.exceptions.OrderNotFoundException
import com.rj.ecommerce_backend.order.mapper.OrderMapper
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.order.service.OrderService
import com.rj.ecommerce_backend.sorting.OrderSortField
import com.rj.ecommerce_backend.sorting.SortValidator
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
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController


@RestController
@RequestMapping("/api/v1/admin/orders") // Base path for admin order operations
@PreAuthorize("hasRole('ADMIN')") // Secure all endpoints in this controller by default
class OrderAdminController(
    private val orderService: OrderService,
    private val sortValidator: SortValidator,
    private val orderMapper: OrderMapper
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping
    fun getAllOrders(
        // Use the existing OrderSearchCriteria DTO directly for request parameters.
        // Spring will bind query parameters to its fields.
        // Add @Valid if OrderSearchCriteria has validation annotations.
        orderSearchCriteria: OrderSearchCriteria,

        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<OrderDTO>> {
        logger.info {
            "Admin request to get all orders. Criteria: $orderSearchCriteria, " +
                    "Page: $page, Size: $size, Sort: '$sort'"
        }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort,
            OrderSortField::class.java
        )
        val pageable = PageRequest.of(page, size, validatedSort)

        // OrderSearchCriteria now directly contains all filter fields including userId (nullable for admin).
        // The OrderService.getAllOrders method will use these criteria.
        val ordersPage = orderService.getAllOrders(pageable, orderSearchCriteria)

        logger.info { "Admin retrieved ${ordersPage.numberOfElements} orders on page $page out of ${ordersPage.totalElements} total." }
        return ResponseEntity.ok(ordersPage)
    }

    @GetMapping("/{orderId}")
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<OrderDTO> {
        logger.info { "Admin request to get order by ID: $orderId" }

        val order = orderService.getOrderByIdAdmin(orderId) ?: throw OrderNotFoundException(orderId)
        val orderDto = orderMapper.toDto(order)
        logger.info { "Admin successfully retrieved order ID: $orderId" }
        return ResponseEntity.ok(orderDto)
    }

    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Long,
        @Valid @RequestBody statusUpdateRequest: OrderStatusUpdateRequestDTO
    ): ResponseEntity<OrderDTO> {
        logger.info { "Admin request to update status for order ID: $orderId to ${statusUpdateRequest.newStatus}" }
        val updatedOrderDto = orderService.updateOrderStatus(orderId, statusUpdateRequest.newStatus)
        logger.info { "Admin successfully updated status for order ID: $orderId" }
        return ResponseEntity.ok(updatedOrderDto)
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelOrderAsAdmin(@PathVariable orderId: Long) {
        logger.info { "Admin request to cancel order ID: $orderId" }
        orderService.cancelOrderAdmin(orderId)
        logger.info { "Admin successfully processed cancellation for order ID: $orderId" }
    }
}