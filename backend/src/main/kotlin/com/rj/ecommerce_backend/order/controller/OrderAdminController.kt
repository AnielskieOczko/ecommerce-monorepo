package com.rj.ecommerce_backend.order.controller

import com.rj.ecommerce.api.shared.dto.order.OrderDTO
import com.rj.ecommerce.api.shared.dto.order.OrderStatusUpdateRequestDTO
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.search.OrderSearchCriteria
import com.rj.ecommerce_backend.order.service.OrderCommandService
import com.rj.ecommerce_backend.order.service.OrderQueryService
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
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasRole('ADMIN')")
class OrderAdminController(
    private val orderQueryService: OrderQueryService,
    private val orderCommandService: OrderCommandService,
    private val sortValidator: SortValidator
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    @GetMapping
    fun getAllOrders(
        orderSearchCriteria: OrderSearchCriteria,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id:asc") sort: String?
    ): ResponseEntity<Page<OrderDTO>> {
        logger.info { "Admin request to get all orders. Criteria: $orderSearchCriteria, Page: $page, Size: $size, Sort: '$sort'" }
        val validatedSort: Sort = sortValidator.validateAndBuildSort(sort, OrderSortField::class.java)
        val pageable = PageRequest.of(page, size, validatedSort)

        val ordersPage = orderQueryService.getAllOrders(pageable, orderSearchCriteria)

        logger.info { "Admin retrieved ${ordersPage.numberOfElements} orders on page $page out of ${ordersPage.totalElements} total." }
        return ResponseEntity.ok(ordersPage)
    }

    @GetMapping("/{orderId}")
    fun getOrderById(@PathVariable orderId: Long): ResponseEntity<OrderDTO> {
        logger.info { "Admin request to get order by ID: $orderId" }

        val orderDto = orderQueryService.getOrderByIdAdmin(orderId)
            ?: throw OrderNotFoundException(orderId)

        logger.info { "Admin successfully retrieved order ID: $orderId" }
        return ResponseEntity.ok(orderDto)
    }

    @PutMapping("/{orderId}/status")
    fun updateOrderStatus(
        @PathVariable orderId: Long,
        @Valid @RequestBody statusUpdateRequest: OrderStatusUpdateRequestDTO
    ): ResponseEntity<OrderDTO> {
        logger.info { "Admin request to update status for order ID: $orderId to ${statusUpdateRequest.newStatus}" }

        val updatedOrderDto = orderCommandService.updateOrderStatus(orderId, statusUpdateRequest.newStatus)

        logger.info { "Admin successfully updated status for order ID: $orderId" }
        return ResponseEntity.ok(updatedOrderDto)
    }

    @DeleteMapping("/{orderId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun cancelOrderAsAdmin(@PathVariable orderId: Long) {
        logger.info { "Admin request to cancel order ID: $orderId" }

        orderCommandService.cancelOrderAdmin(orderId)

        logger.info { "Admin successfully processed cancellation for order ID: $orderId" }
    }
}