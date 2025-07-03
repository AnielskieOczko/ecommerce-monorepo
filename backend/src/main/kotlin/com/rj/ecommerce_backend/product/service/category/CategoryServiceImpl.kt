package com.rj.ecommerce_backend.product.service.category

import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import com.rj.ecommerce.api.shared.dto.product.category.CategoryRequestDTO
import com.rj.ecommerce_backend.product.domain.Category
import com.rj.ecommerce_backend.product.exception.CategoryAlreadyExistsException
import com.rj.ecommerce_backend.product.exception.CategoryInUseException
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.repository.CategoryRepository
import com.rj.ecommerce_backend.product.repository.ProductRepository
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import io.github.oshai.kotlinlogging.KotlinLogging

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CategoryServiceImpl(
    private val categoryRepository: CategoryRepository,
    private val productRepository: ProductRepository

) : CategoryService {

    companion object {
        private val logger = KotlinLogging.logger { }
        private const val CATEGORY_NOT_FOUND_MSG_PREFIX = "Category not found with ID: "
    }

    override fun createCategory(requestDTO: CategoryRequestDTO): CategoryDTO {
        logger.info { "Attempting to create category with name: '${requestDTO.name}'" }

        if (requestDTO.name.isBlank()) {
            logger.warn { "Attempt to create category with blank name." }
            throw IllegalArgumentException("Category name cannot be blank.")
        }

        categoryRepository.findByNameIgnoreCase(requestDTO.name)?.let {
            logger.warn { "Attempt to create duplicate category name: '${requestDTO.name}'" }
            throw CategoryAlreadyExistsException("Category with name '${requestDTO.name}' already exists.")
        }

        val categoryEntity = Category(name = requestDTO.name)
        val savedCategory = categoryRepository.save(categoryEntity)
        logger.info { "Category with name '${savedCategory.name}' created successfully with ID: ${savedCategory.id}" }

        return mapEntityToDTO(savedCategory)
    }

    @Transactional(readOnly = true)
    override fun getCategoryById(categoryId: Long): CategoryDTO {
        logger.debug { "Fetching category by ID: $categoryId" }
        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                logger.warn { "$CATEGORY_NOT_FOUND_MSG_PREFIX$categoryId" }
                CategoryNotFoundException(categoryId)
            }
        return mapEntityToDTO(category)
    }

    @Transactional(readOnly = true)
    override fun getAllCategories(
        pageable: Pageable,
        criteria: CategorySearchCriteria
    ): Page<CategoryDTO> {
        logger.debug { "Fetching all categories with pageable: $pageable and criteria: $criteria" }

        val spec: Specification<Category> = criteria.toSpecification()
        val categoriesPage: Page<Category> = categoryRepository.findAll(spec, pageable)

        return categoriesPage.map { category -> mapEntityToDTO(category) }
    }

    @Transactional(readOnly = true)
    override fun getCategoryNames(): List<String> {
        logger.debug { "Fetching all category names." }
        return categoryRepository.findAll().mapNotNull { it.name }
    }

    override fun updateCategory(
        categoryId: Long,
        request: CategoryRequestDTO
    ): CategoryDTO {
        logger.info { "Attempting to update category ID: $categoryId with new name: '${request.name}'" }
        if (request.name.isBlank()) {
            logger.warn { "Attempt to update category ID: $categoryId with blank name." }
            throw IllegalArgumentException("Category name cannot be blank for update.")
        }

        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                logger.warn { "Update failed: $CATEGORY_NOT_FOUND_MSG_PREFIX$categoryId" }
                CategoryNotFoundException(categoryId)
            }

        categoryRepository.findByNameIgnoreCase(request.name)?.let { existingCategoryWithNewName ->
            if (existingCategoryWithNewName.id != categoryId) {
                logger.warn { "Update failed for category ID $categoryId: New name '${request.name}' already exists for category ID ${existingCategoryWithNewName.id}." }
                throw CategoryAlreadyExistsException("Category name '${request.name}' is already in use by another category.")
            }
        }

        category.name = request.name
        val updatedCategory = categoryRepository.save(category)
        logger.info { "Category ID: $categoryId updated successfully to name: '${updatedCategory.name}'" }
        return mapEntityToDTO(updatedCategory)
    }

    override fun deleteCategory(categoryId: Long) {
        logger.info { "Attempting to delete category ID: $categoryId" }
        val category = categoryRepository.findById(categoryId)
            .orElseThrow {
                logger.warn { "$CATEGORY_NOT_FOUND_MSG_PREFIX$categoryId" }
                CategoryNotFoundException(categoryId)
            }

        val productCount = productRepository.countByCategories_Id(category.id!!)

        if (productCount > 0) {
            logger.warn { "Deletion failed for category ID $categoryId ('${category.name}'): Category is associated with $productCount product(s)." }
            throw CategoryInUseException("Cannot delete category '${category.name}' as it is currently associated with $productCount product(s).")
        }

        categoryRepository.delete(category)
        logger.info { "Category ID: $categoryId named '${category.name}' deleted successfully." }
    }

    private fun mapEntityToDTO(category: Category): CategoryDTO {
        return CategoryDTO(
            id = category.id ?: throw IllegalStateException("Category entity must have an ID to be mapped to DTO."),
            name = category.name
        )
    }

}