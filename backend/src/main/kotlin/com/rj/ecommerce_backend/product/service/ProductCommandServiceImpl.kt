package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce_backend.api.shared.core.QuantityInStock
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.product.repository.ProductRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service


private val logger = KotlinLogging.logger { ProductCommandServiceImpl::class }

@Service
class ProductCommandServiceImpl(
    private val productRepository: ProductRepository,
) : ProductCommandService {

    override fun reduceProductQuantity(productId: Long, quantityInStockToReduce: Int) {
        logger.debug { "Reducing stock for product ID: $productId by $quantityInStockToReduce" }

        require(quantityInStockToReduce > 0) { "Quantity to reduce must be positive." }


        val product = productRepository.findById(productId)
            .orElseThrow { ProductNotFoundException(productId) }

        val currentStock = product.quantityInStock.value

        if (currentStock < quantityInStockToReduce) {
            throw InsufficientStockException("Insufficient stock for product $productId. Current: $currentStock, Requested reduction: $quantityInStockToReduce")
        }

        val newQuantity = currentStock - quantityInStockToReduce
        // This custom repository method is fine if it directly updates the DB field.
        // Otherwise, update the entity and save:
        // product.quantityInStock = QuantityInStock(newQuantity)
        // productRepository.save(product)
        productRepository.updateProductQuantity(
            productId,
            QuantityInStock(newQuantity)
        )
        logger.info { "Reduced stock for product ID: $productId. New stock: $newQuantity" }
    }

}