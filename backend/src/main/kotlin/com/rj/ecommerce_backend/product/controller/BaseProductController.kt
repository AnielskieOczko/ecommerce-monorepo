package com.rj.ecommerce_backend.product.controller

import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import com.rj.ecommerce_backend.product.service.FileStorageService
import com.rj.ecommerce_backend.product.service.ProductService
import com.rj.ecommerce_backend.sorting.ProductSortField
import com.rj.ecommerce_backend.sorting.SortValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

abstract class BaseProductController(
    protected val productService: ProductService,
    protected val fileStorageService: FileStorageService,
    protected val sortValidator: SortValidator
) {

    companion object {
        @JvmStatic
        protected val logger = KotlinLogging.logger { }
    }

    @GetMapping
    open fun getAllProducts(
        productSearchCriteria: ProductSearchCriteria,
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<ProductResponseDTO>> {
        logger.info {
            "Request to get all products. Criteria: $productSearchCriteria, " +
                    "Page: $page, Size: $size, Sort: '$sort'"
        }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort, ProductSortField::class.java
        )
        val pageable = PageRequest.of(page, size, validatedSort)

        val productsPage = productService.getAllProducts(pageable, productSearchCriteria)
        logger.debug { "Retrieved ${productsPage.numberOfElements} products on page $page. Total: ${productsPage.totalElements}" }
        return ResponseEntity.ok(productsPage)
    }

    @GetMapping("/images/{fileName:.+}") // Added regex ":.+" to handle filenames with dots
    open fun getProductImage(@PathVariable fileName: String): ResponseEntity<Resource> { // 'open' if overridable
        logger.info { "Request to get product image: '$fileName'" }
        val resource = fileStorageService.loadFileAsResource(fileName)

        // Try to determine content type dynamically for better browser handling
        var contentType: MediaType? = null
        try {
            contentType = MediaType.parseMediaType(java.nio.file.Files.probeContentType(resource.file.toPath()))
        } catch (e: Exception) {
            logger.warn { "Could not determine content type for file '$fileName'. Defaulting to application/octet-stream." }
            contentType = MediaType.APPLICATION_OCTET_STREAM // Fallback
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            // Optional: Add Content-Disposition header to suggest filename for download
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            // For images, "inline" is usually preferred over "attachment".
            .body(resource)
    }
}