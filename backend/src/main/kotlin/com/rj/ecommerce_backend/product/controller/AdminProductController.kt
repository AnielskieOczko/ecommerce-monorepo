package com.rj.ecommerce_backend.product.controller

import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce.api.shared.dto.product.ProductUpdateRequestDTO

// Backend components
import com.rj.ecommerce_backend.product.service.ProductService
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.sorting.ProductSortField
import com.rj.ecommerce_backend.sorting.SortValidator
import com.rj.ecommerce_backend.storage.service.LocalStorageService

// Swagger/OpenAPI
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag


// Spring & Jakarta
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Tag(name = "Admin Product Management", description = "Administrative APIs for product management")
@SecurityRequirement(name = "bearerAuth") // For Swagger UI if using Bearer token auth
@RestController
@RequestMapping("/api/v1/admin/products")
@PreAuthorize("hasRole('ADMIN')")
class AdminProductController(
    productService: ProductService,
    localStorageService: LocalStorageService,
    sortValidator: SortValidator
) : BaseProductController(productService, localStorageService, sortValidator) {

    @Operation(summary = "Get product by ID (Admin)", description = "Retrieves a specific product by its ID for admin.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product found successfully"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    @GetMapping("/{productId}")
    fun getProductByIdAdmin(
        @Parameter(description = "ID of the product to retrieve") @PathVariable productId: Long
    ): ResponseEntity<ProductResponseDTO> {
        logger.info { "Admin request to get product by ID: $productId" }

        val productDto = productService.getProductById(productId)
            ?: throw ProductNotFoundException(productId)
        return ResponseEntity.ok(productDto)
    }

    // getAllProducts is inherited from BaseProductController and secured by class-level @PreAuthorize

    // getProductImage is inherited from BaseProductController and secured by class-level @PreAuthorize

    @Operation(
        summary = "Get products by Category ID (Admin)",
        description = "Retrieves products belonging to a specific category for admin."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Products found or empty list"),
            ApiResponse(responseCode = "404", description = "Category not found (if service throws)")
        ]
    )
    @GetMapping("/category/{categoryId}")
    fun getProductsByCategoryIdAdmin(
        @Parameter(description = "ID of the category") @PathVariable categoryId: Long,
        @Parameter(description = "Page number, 0-indexed") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Sort parameters (e.g., name:asc or price:desc)")
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<ProductResponseDTO>> {
        logger.info { "Admin request for products in category ID: $categoryId, Page: $page, Size: $size, Sort: $sort" }

        val validatedSort = sortValidator.validateAndBuildSort(sort, ProductSortField::class.java)
        val pageable: Pageable = PageRequest.of(page, size, validatedSort)

        val productPage = productService.findProductsByCategory(categoryId, pageable)
        return ResponseEntity.ok(productPage)
    }

    @Operation(summary = "Search products by name (Admin)", description = "Searches products by name for admin.")
    @GetMapping("/search")
    fun searchProductsAdmin(
        @Parameter(description = "Product name search term")
        @RequestParam(name = "name") productName: String,
        @Parameter(description = "Page number, 0-indexed") @RequestParam(defaultValue = "0") page: Int,
        @Parameter(description = "Number of items per page") @RequestParam(defaultValue = "10") size: Int,
        @Parameter(description = "Sort parameters (e.g., name:asc or price:desc)")
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<ProductResponseDTO>> {
        logger.info { "Admin product search by name: '$productName', Page: $page, Size: $size, Sort: $sort" }

        val validatedSort = sortValidator.validateAndBuildSort(sort, ProductSortField::class.java)
        val pageable: Pageable = PageRequest.of(page, size, validatedSort)

        val searchResults = productService.findProductsByName(productName, pageable)
        return ResponseEntity.ok(searchResults)
    }


    @Operation(summary = "Create new product (Admin)", description = "Creates a new product with optional images.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "Product created successfully"),
            ApiResponse(responseCode = "400", description = "Invalid product data provided")
        ]
    )
    @PostMapping(consumes = [MediaType.MULTIPART_FORM_DATA_VALUE]) // Explicitly state consumes
    fun createProduct(
        @Parameter(description = "Product data (JSON part)")
        @RequestPart("productData") @Valid productCreateDTO: ProductCreateRequestDTO,
        @Parameter(description = "Product images (optional file parts)")
        @RequestPart(value = "imageFiles", required = false) imageFiles: List<MultipartFile>?
    ): ResponseEntity<ProductResponseDTO> {
        logger.info { "Admin request to create product: ${productCreateDTO.productData.name} with ${imageFiles?.size ?: 0} images." }
        val createdProduct = productService.createProduct(productCreateDTO, imageFiles ?: emptyList())

        val location: URI = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdProduct.id)
            .toUri()

        logger.info { "Admin successfully created product. ID: ${createdProduct.id}, Location: $location" }
        return ResponseEntity.created(location).body(createdProduct)
    }

    @Operation(
        summary = "Update existing product's core data (Admin)",
        description = "Updates an existing product's core attributes like name, description, price, categories. Images are managed via separate endpoints."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Product updated successfully"),
            ApiResponse(responseCode = "400", description = "Invalid product data provided"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )

    @PutMapping(value = ["/{productId}"], consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateProductCoreDetails(
        @Parameter(description = "ID of the product to update") @PathVariable productId: Long,
        @Parameter(description = "Product update data (JSON)")
        @Valid @RequestBody productUpdateDTO: ProductUpdateRequestDTO // No @RequestPart needed for files
    ): ResponseEntity<ProductResponseDTO> {
        logger.info { "Admin request to update core details for product ID: $productId" }

        val updatedProduct = productService.updateProduct(productId, productUpdateDTO)

        logger.info { "Admin successfully updated core details for product ID: $productId." }
        return ResponseEntity.ok(updatedProduct)
    }

    @Operation(summary = "Delete product image (Admin)", description = "Deletes a specific image from a product.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Image deleted successfully"),
            ApiResponse(responseCode = "404", description = "Product or Image not found")
        ]
    )
    @DeleteMapping("/{productId}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProductImage(
        @Parameter(description = "ID of the product") @PathVariable productId: Long,
        @Parameter(description = "ID of the image to delete") @PathVariable imageId: Long
    ) {
        logger.info { "Admin request to delete image ID: $imageId from product ID: $productId" }
        productService.deleteProductImage(productId, imageId)
        logger.info { "Admin successfully processed delete request for image ID: $imageId on product ID: $productId" }
    }

    @Operation(summary = "Delete product (Admin)", description = "Deletes a product by its ID.")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "Product deleted successfully"),
            ApiResponse(responseCode = "404", description = "Product not found")
        ]
    )
    @DeleteMapping("/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteProduct(@Parameter(description = "ID of the product to delete") @PathVariable productId: Long) {
        logger.info { "Admin request to delete product ID: $productId" }
        productService.deleteProduct(productId)
        logger.info { "Admin successfully processed delete request for product ID: $productId" }
    }
}