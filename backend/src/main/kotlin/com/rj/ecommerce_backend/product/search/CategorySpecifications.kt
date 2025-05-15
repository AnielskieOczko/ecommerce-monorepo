package com.rj.ecommerce_backend.product.search

import com.rj.ecommerce_backend.product.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.Specification


private val logger = KotlinLogging.logger { }

object CategorySpecifications {

    fun withSearchCriteria(search: String?): Specification<Category>? {
        if (search.isNullOrBlank()) return null

        val searchLower = "%${search.lowercase()}%"
        logger.debug { "Applying category search criteria: '$search'" }

        return Specification { root, _, cb ->
            try {
                val searchIdLong = search.toLong()
                cb.or(
                    cb.equal(root.get<Long>("id"), searchIdLong),
                    cb.like(cb.lower(root.get("name")), searchLower)
                )
            } catch (e: NumberFormatException) {
                cb.like(cb.lower(root.get("name")), searchLower)
            }
        }
    }
}