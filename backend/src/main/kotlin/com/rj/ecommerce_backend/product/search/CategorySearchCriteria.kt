package com.rj.ecommerce_backend.product.search

import com.rj.ecommerce_backend.product.domain.Category
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.Specification

data class CategorySearchCriteria(
    val search: String?,
    val name: String?
) {

    companion object {
        private val logger = KotlinLogging.logger {}
    }

    fun toSpecification(): Specification<Category> {
        logger.debug { "Building category specification with criteria: search='${search}', name='${name}'" }

        return Specification
            .where(CategorySpecifications.withSearchCriteria(search))
    }
}