package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce_backend.api.shared.dto.product.response.ProductResponse
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductQueryService {
    fun getProductById(productId: Long): ProductResponse?
    fun getAllProducts(pageable: Pageable, criteria: ProductSearchCriteria): Page<ProductResponse>
    fun findProductEntitiesByIds(productIds: List<Long>): List<Product>
    fun findProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponse>
    fun findProductsByName(name: String, pageable: Pageable): Page<ProductResponse>
    fun getProductEntityForValidation(productId: Long): Product?
}