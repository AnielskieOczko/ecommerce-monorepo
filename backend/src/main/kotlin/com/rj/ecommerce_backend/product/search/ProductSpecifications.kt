package com.rj.ecommerce_backend.product.search

import com.rj.ecommerce.api.shared.core.ProductName
import com.rj.ecommerce.api.shared.core.Money
import com.rj.ecommerce.api.shared.core.QuantityInStock
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.domain.Product
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.criteria.Join
import jakarta.persistence.criteria.JoinType
import org.springframework.data.jpa.domain.Specification
import java.math.BigDecimal
import java.math.RoundingMode


private val logger = KotlinLogging.logger {}

object ProductSpecifications {

    fun withSearchCriteria(search: String?): Specification<Product>? {
        if (search.isNullOrBlank()) {
            return null
        }
        logger.debug { "Applying product search criteria: '$search'" }

        return Specification { root, _, cb ->
            val searchLowerCase = "%${search.lowercase()}%"
            val productNamePath = root.get<ProductName>("productName").get<String>("value")

            try {
                val searchId = search.toLong()
                cb.or(
                    cb.equal(root.get<Long>("id"), searchId),
                    cb.like(cb.lower(productNamePath), searchLowerCase)
                )

            } catch (e: NumberFormatException) {
                cb.like(cb.lower(productNamePath), searchLowerCase)
            }
        }
    }

    fun withCategory(categoryIdString: String?): Specification<Product>? {
        if (categoryIdString.isNullOrBlank()) return null


        return Specification { root, _, cb ->
            try {
                val categoryIdLong = categoryIdString.toLong()
                logger.debug { "Filtering by product category ID: $categoryIdLong" }
                val categoryJoin: Join<Product, Category> = root.join("categories", JoinType.INNER)
                cb.equal(categoryJoin.get<Long>("id"), categoryIdLong)
            } catch (e: NumberFormatException) {
                logger.warn(e) { "Invalid categoryId format: '$categoryIdString'. Skipping category filter." }
                null
            }
        }

    }

    fun withPriceRange(min: BigDecimal?, max: BigDecimal?): Specification<Product>? {
        if (min ==null && max == null) return null


        return Specification { root, _, cb ->
            val minPrice = min?.setScale(2, RoundingMode.FLOOR)
            val maxPrice = max?.setScale(2, RoundingMode.CEILING)
            logger.debug { "Filtering by product price: min=${minPrice ?: "N/A"}, max=${maxPrice ?: "N/A"}" }

            // 1. Get the Path to the 'unitPrice' attribute (which is of type Money)
            // 2. From that Path<Money>, get the Path to its 'amount' attribute (which is BigDecimal)
            val priceAmountPath = root
                .get<Money>("unitPrice")  // This is Path<Money>
                .get<BigDecimal>("amount") // This is Path<BigDecimal>

            when {
                minPrice != null && maxPrice != null -> cb.between(priceAmountPath, minPrice, maxPrice)
                minPrice != null -> cb.greaterThanOrEqualTo(priceAmountPath, minPrice)
                maxPrice != null -> cb.lessThanOrEqualTo(priceAmountPath, maxPrice)
                else -> null
            }
        }
    }

    fun withStockQuantityRange(min: Int?, max: Int?): Specification<Product>? {
        if (min == null && max != null ) return null

        return Specification { root, _, cb ->
            logger.debug { "Filtering by stock quantity: min=${min ?: "N/A"}, max=${max ?: "N/A"}" }
            val quantityInStockPath = root
                .get<QuantityInStock>("quantityInStock")
                .get<Int>("value")

            when {
                min != null && max != null -> cb.between(quantityInStockPath, min, max)
                min != null -> cb.greaterThanOrEqualTo(quantityInStockPath, min)
                max != null -> cb.lessThanOrEqualTo(quantityInStockPath, max)
                else -> null
            }
        }

    }


}