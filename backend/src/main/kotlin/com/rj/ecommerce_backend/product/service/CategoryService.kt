package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce.api.shared.dto.product.CategoryCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.product.CategoryDTO
import com.rj.ecommerce.api.shared.dto.product.CategoryUpdateDTO
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CategoryService {
    fun createCategory(requestDTO: CategoryCreateRequestDTO): CategoryDTO
    fun getCategoryById(categoryId: Long): CategoryDTO
    fun getAllCategories(pageable: Pageable, criteria: CategorySearchCriteria): Page<CategoryDTO>
    fun getCategoryNames(): List<String>
    fun updateCategory(categoryId: Long, request: CategoryUpdateDTO): CategoryDTO
    fun deleteCategory(categoryId: Long)
}