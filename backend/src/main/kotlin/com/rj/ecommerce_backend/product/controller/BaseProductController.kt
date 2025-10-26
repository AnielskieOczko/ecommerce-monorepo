package com.rj.ecommerce_backend.product.controller

import com.rj.ecommerce.api.shared.dto.product.response.ProductResponse
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import com.rj.ecommerce_backend.product.service.ProductQueryService
import com.rj.ecommerce_backend.sorting.ProductSortField
import com.rj.ecommerce_backend.sorting.SortValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

abstract class BaseProductController(
    protected val productQueryService: ProductQueryService,
    protected val sortValidator: SortValidator
) {
    companion object {
        @JvmStatic
        protected val logger = KotlinLogging.logger {}
    }

    @GetMapping
    open fun getAllProducts(
        productSearchCriteria: ProductSearchCriteria,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id:asc") sort: String?
    ): ResponseEntity<Page<ProductResponse>> {
        logger.info { "Request to get all products. Criteria: $productSearchCriteria, Page: $page, Size: $size, Sort: '$sort'" }
        val validatedSort: Sort = sortValidator.validateAndBuildSort(sort, ProductSortField::class.java)
        val pageable = PageRequest.of(page, size, validatedSort)
        val productsPage = productQueryService.getAllProducts(pageable, productSearchCriteria)
        logger.debug { "Retrieved ${productsPage.numberOfElements} products on page $page. Total: ${productsPage.totalElements}" }
        return ResponseEntity.ok(productsPage)
    }
}