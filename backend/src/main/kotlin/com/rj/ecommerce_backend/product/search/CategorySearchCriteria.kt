package com.rj.ecommerce_backend.product.dto

import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.search.CategorySpecifications
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