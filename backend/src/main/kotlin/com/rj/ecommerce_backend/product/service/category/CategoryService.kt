package com.rj.ecommerce_backend.product.service.category

import com.rj.ecommerce.api.shared.dto.product.common.CategoryDetails
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CategoryService {
    fun createCategory(requestDTO: CategoryDetails): CategoryDetails
    fun getCategoryById(categoryId: Long): CategoryDetails
    fun getAllCategories(pageable: Pageable, criteria: CategorySearchCriteria): Page<CategoryDetails>
    fun getCategoryNames(): List<String>
    fun updateCategory(categoryId: Long, request: CategoryDetails): CategoryDetails
    fun deleteCategory(categoryId: Long)
}