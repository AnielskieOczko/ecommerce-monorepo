package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce.api.shared.dto.product.ProductResponseDTO
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.search.ProductSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface ProductQueryService {
    fun getProductById(productId: Long): ProductResponseDTO?
    fun getAllProducts(pageable: Pageable, criteria: ProductSearchCriteria): Page<ProductResponseDTO>
    fun findProductsByCategory(categoryId: Long, pageable: Pageable): Page<ProductResponseDTO>
    fun findProductsByName(name: String, pageable: Pageable): Page<ProductResponseDTO>
    fun getProductEntityForValidation(productId: Long): Product?
}