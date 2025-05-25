package com.rj.ecommerce_backend.product.repository

import com.rj.ecommerce_backend.product.domain.Category
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<Category, Long>,
    JpaSpecificationExecutor<Category> {
    fun findByName(name: String): Category?

    fun findByNameIgnoreCase(name: String): Category?
}