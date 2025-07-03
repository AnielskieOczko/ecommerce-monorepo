package com.rj.ecommerce_backend.product.usecases

import com.rj.ecommerce_backend.order.repository.OrderItemRepository
import com.rj.ecommerce_backend.product.exception.ProductInUseException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.service.image.ProductImageService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

private val log = KotlinLogging.logger {}

@Component
class DeleteProductUseCase(
    private val productRepository: ProductRepository,
    private val orderItemRepository: OrderItemRepository,
    private val productImageService: ProductImageService
) {
    @Transactional
    fun execute(productId: Long) {
        log.warn { "Attempting to execute deletion for product ID: $productId" }

        // 1. Check for business rule violations BEFORE fetching the full entity.
        if (orderItemRepository.existsByProductId(productId)) {
            throw ProductInUseException("Cannot delete product ID: $productId. It is referenced by existing orders.")
        }

        // 2. If clear, fetch the product entity.
        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        // 3. Delegate image deletion to the specialized service.
        // This will handle file storage cleanup.
        productImageService.deleteAllImagesForProduct(product)

        // 4. After all related assets are handled, delete the product itself.
        // We re-fetch to ensure the images collection is empty before the final delete.
        val productToFinallyDelete = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) } // Should not happen, but safe

        productRepository.delete(productToFinallyDelete)

        log.info { "Successfully and safely deleted product ID: $productId" }
    }
}