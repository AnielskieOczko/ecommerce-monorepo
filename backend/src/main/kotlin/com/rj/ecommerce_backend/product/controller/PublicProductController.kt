package com.rj.ecommerce_backend.product.controller

// Shared DTOs
import com.rj.ecommerce_backend.api.shared.dto.product.response.ProductResponse

// Backend components
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.service.ProductQueryService
import com.rj.ecommerce_backend.sorting.ProductSortField
import com.rj.ecommerce_backend.sorting.SortValidator
import com.rj.ecommerce_backend.storage.service.StorageService

// Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.io.Resource


import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Files

@Tag(name = "Public Product API", description = "Public APIs for viewing products")
@RestController
@RequestMapping("/api/v1/public/products")
class PublicProductController(
    productQueryService: ProductQueryService,
    sortValidator: SortValidator,
    private val storageService: StorageService
) : BaseProductController(productQueryService, sortValidator) {


    // getAllProducts() is inherited from BaseProductController.
    // getProductImage() is inherited from BaseProductController.

    // Example: Specific public endpoint to get a product by ID
    @Operation(summary = "Get public product by ID", description = "Retrieves a publicly available product by its ID.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product found"),
            ApiResponse(responseCode = "404", description = "Product not found or not publicly available")
        ]
    )
    @GetMapping("/{productId}")
    fun getPublicProductById(
        @Parameter(description = "ID of the product to retrieve") @PathVariable productId: Long
    ): ResponseEntity<ProductResponse> {
        logger.info { "Public request for product ID: $productId" }
        val productDto = productQueryService.getProductById(productId)
            ?: throw ProductNotFoundException(productId)
        return ResponseEntity.ok(productDto)
    }

    @Operation(
        summary = "Get public products by Category ID",
        description = "Retrieves publicly available products belonging to a category."
    )
    @GetMapping("/category/{categoryId}")
    fun getPublicProductsByCategoryId(
        @Parameter(description = "ID of the category") @PathVariable categoryId: Long,
        @Parameter(description = "Page number, 0-indexed") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Sort parameters (e.g., name:asc)")
        @RequestParam(defaultValue = "name:asc", required = false) sort: String?
    ): ResponseEntity<Page<ProductResponse>> {
        logger.info { "Public request for products in category ID: $categoryId, Page: $page, Size: $size, Sort: $sort" }

        val validatedSort = sortValidator.validateAndBuildSort(sort, ProductSortField::class.java)
        val pageable: Pageable = PageRequest.of(page, size, validatedSort)

        val productPage = productQueryService.findProductsByCategory(categoryId, pageable)
        return ResponseEntity.ok(productPage)
    }

    @Operation(
        summary = "Search public products by name",
        description = "Searches publicly available products by name."
    )
    @GetMapping("/search")
    fun searchPublicProducts(
        @Parameter(description = "Product name search term")
        @RequestParam(name = "name") productName: String,
        @Parameter(description = "Page number, 0-indexed") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Sort parameters")
        @RequestParam(defaultValue = "name:asc", required = false) sort: String?
    ): ResponseEntity<Page<ProductResponse>> {
        logger.info { "Public product search by name: '$productName', Page: $page, Size: $size, Sort: $sort" }

        val validatedSort = sortValidator.validateAndBuildSort(sort, ProductSortField::class.java)
        val pageable: Pageable = PageRequest.of(page, size, validatedSort)

        val searchResults = productQueryService.findProductsByName(productName, pageable)
        return ResponseEntity.ok(searchResults)
    }

    @GetMapping("/images/{fileName:.+}")
    fun getProductImage(@PathVariable fileName: String): ResponseEntity<Resource> {
        logger.info { "Request to get public product image: '$fileName'" }
        val resource = storageService.loadAsResource(fileName)

        val contentType = try {
            MediaType.parseMediaType(Files.probeContentType(resource.file.toPath()))
        } catch (e: Exception) {
            // Fallback if the MIME type cannot be determined.
            logger.warn { "Could not determine content type for file '$fileName'. Defaulting to application/octet-stream." }
            MediaType.APPLICATION_OCTET_STREAM
        }

        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"${resource.filename}\"")
            .body(resource)
    }
}