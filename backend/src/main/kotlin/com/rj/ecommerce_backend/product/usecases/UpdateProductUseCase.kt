package com.rj.ecommerce_backend.product.usecases

import com.rj.ecommerce.api.shared.dto.product.request.ProductUpdateRequest
import com.rj.ecommerce.api.shared.dto.product.response.ProductResponse
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.mapper.ProductMapper
import com.rj.ecommerce_backend.product.repository.CategoryRepository
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.service.image.ProductImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Component
class UpdateProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val productImageService: ProductImageService,
    private val productMapper: ProductMapper
) {
    @Transactional
    fun execute(productId: Long, dto: ProductUpdateRequest): ProductResponse {
        logger.info { "Executing UpdateProductUseCase for product ID: $productId" }

        // 1. Fetch the primary entity
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        // 2. Fetch related entities if an update is requested
        val categoriesToUpdate = dto.categoryIds?.let { ids ->
            if (ids.isNotEmpty()) {
                val fetched = categoryRepository.findAllById(ids)
                if (fetched.size != ids.size) {
                    throw CategoryNotFoundException("One or more categories not found for IDs: $ids")
                }
                fetched
            } else {
                emptyList() // An empty list means "remove all categories"
            }
        }

        // 3. Use the pure mapper to apply changes
        productMapper.updateEntityFromDto(product, dto, categoriesToUpdate)

        // 4. Save the updated product
        val savedProduct = productRepository.save(product)

        // 5. Get the final state of the images and create the response DTO
        val imageInfos = productImageService.getImageInfosForProduct(savedProduct)

        logger.info { "Successfully updated product ID: ${savedProduct.id}" }
        return productMapper.toDto(savedProduct, imageInfos)
    }
}