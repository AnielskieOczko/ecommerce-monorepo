package com.rj.ecommerce_backend.product.service.category

import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import com.rj.ecommerce.api.shared.dto.product.category.CategoryRequestDTO
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CategoryService {
    fun createCategory(requestDTO: CategoryRequestDTO): CategoryDTO
    fun getCategoryById(categoryId: Long): CategoryDTO
    fun getAllCategories(pageable: Pageable, criteria: CategorySearchCriteria): Page<CategoryDTO>
    fun getCategoryNames(): List<String>
    fun updateCategory(categoryId: Long, request: CategoryRequestDTO): CategoryDTO
    fun deleteCategory(categoryId: Long)
}