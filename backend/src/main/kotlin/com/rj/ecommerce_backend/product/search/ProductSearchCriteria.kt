package com.rj.ecommerce_backend.product.search

import com.rj.ecommerce_backend.product.domain.Product
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal



data class ProductSearchCriteria(
    val search: String?,
    val categoryId: String?,
    val minPrice: BigDecimal?,
    val maxPrice: BigDecimal?,
    val minStockQuantity: Int?,
    val maxStockQuantity: Int?
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun toSpecification(): Specification<Product> {
        logger.debug {
            "Building specification with criteria: search='${search}', categoryId=${categoryId}, " +
                    "price=(${minPrice} - ${maxPrice}), stock=(${minStockQuantity} - ${maxStockQuantity})"
        }
        return Specification
            .where(ProductSpecifications.withSearchCriteria(search))
            .and(
                ProductSpecifications.withCategory(categoryId)
                    ?.and(ProductSpecifications.withPriceRange(minPrice, maxPrice))
                    ?.and(ProductSpecifications.withStockQuantityRange(minStockQuantity, maxStockQuantity)))
    }
}