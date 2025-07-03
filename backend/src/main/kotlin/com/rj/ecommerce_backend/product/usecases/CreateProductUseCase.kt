package com.rj.ecommerce_backend.product.usecases

import com.rj.ecommerce.api.shared.dto.product.ProductCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.mapper.ProductMapper
import com.rj.ecommerce_backend.product.repository.CategoryRepository
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.service.image.ProductImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

private val logger = KotlinLogging.logger {}

@Component
class CreateProductUseCase(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val productImageService: ProductImageService,
    private val productMapper: ProductMapper
) {
    @Transactional
    fun execute(dto: ProductCreateRequestDTO, images: List<MultipartFile>): ProductResponseDTO {
        logger.info { "Executing CreateProductUseCase for: ${dto.productData.name}" }

        // 1. Fetch and validate related entities
        val categoryIds = dto.productData.categoryIds
        val categories = if (categoryIds.isNotEmpty()) {
            val fetched = categoryRepository.findAllById(categoryIds)
            if (fetched.size != categoryIds.size) {
                throw CategoryNotFoundException("One or more categories not found for IDs: $categoryIds")
            }
            fetched
        } else {
            emptyList()
        }

        // 2. Use the pure mapper to create the initial entity
        val newProduct = productMapper.createEntityFromDto(dto, categories)

        // 3. Persist the product first to get an ID
        val savedProduct = productRepository.save(newProduct)

        // 4. Delegate image handling to the specialized service
        val imageInfos = if (images.isNotEmpty()) {
            productImageService.addImagesToProduct(savedProduct.id!!, images, null)
        } else {
            emptyList()
        }

        logger.info { "Successfully created product ID: ${savedProduct.id}" }

        // 5. Return the final DTO
        return productMapper.toDto(savedProduct, imageInfos)
    }
}