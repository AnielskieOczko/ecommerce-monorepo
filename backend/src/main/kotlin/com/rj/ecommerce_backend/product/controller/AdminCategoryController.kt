package com.rj.ecommerce_backend.product.controller

import com.rj.ecommerce.api.shared.dto.product.category.CategoryDTO
import com.rj.ecommerce.api.shared.dto.product.category.CategoryRequestDTO
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import com.rj.ecommerce_backend.product.service.CategoryService
import com.rj.ecommerce_backend.sorting.CategorySortField
import com.rj.ecommerce_backend.sorting.SortValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@RestController
@RequestMapping("api/v1/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
class AdminCategoryController(
    private val categoryService: CategoryService,
    private val sortValidator: SortValidator
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @PostMapping
    fun createCategory(@RequestBody @Valid requestDTO: CategoryRequestDTO): ResponseEntity<CategoryDTO> {
        logger.info { "Admin request to create category with name: '${requestDTO.name}'" }
        val createdCategory = categoryService.createCategory(requestDTO)

        // Build URI for the newly created resource for the Location header
        val location: URI = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}")
            .buildAndExpand(createdCategory.id)
            .toUri()

        logger.info { "Admin successfully created category. ID: ${createdCategory.id}, Name: ${createdCategory.name}, Location: $location" }
        return ResponseEntity.created(location).body(createdCategory) // Returns 201 Created with Location header
    }

    @GetMapping("/{categoryId}")
    fun getCategoryById(@PathVariable categoryId: Long): ResponseEntity<CategoryDTO> {
        logger.info { "Admin request to get category by ID: $categoryId" }
        val categoryDto = categoryService.getCategoryById(categoryId)
        return ResponseEntity.ok(categoryDto)
    }

    @GetMapping
    fun getAllCategories(
        categorySearchCriteria: CategorySearchCriteria,
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<CategoryDTO>> {
        logger.info {
            "Admin request to get all categories. Criteria: $categorySearchCriteria, " +
                    "Page: $page, Size: $size, Sort: '$sort'"
        }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort,
            CategorySortField::class.java
        )
        val pageable = PageRequest.of(page, size, validatedSort)
        val categoriesPage = categoryService.getAllCategories(pageable, categorySearchCriteria)

        logger.debug { "Admin retrieved ${categoriesPage.numberOfElements} categories on page $page. Total: ${categoriesPage.totalElements}" }
        return ResponseEntity.ok(categoriesPage)
    }

    @PutMapping("/{categoryId}")
    fun updateCategory(
        @PathVariable categoryId: Long,
        @RequestBody @Valid requestDTO: CategoryRequestDTO
    ): ResponseEntity<CategoryDTO> {
        logger.info { "Admin request to update category ID: $categoryId with new name: '${requestDTO.name}'" }
        val updatedCategory = categoryService.updateCategory(categoryId, requestDTO)
        logger.info { "Admin successfully updated category ID: $categoryId to name: '${updatedCategory.name}'" }
        return ResponseEntity.ok(updatedCategory)
    }

    @DeleteMapping("/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCategory(@PathVariable categoryId: Long) {
        logger.info { "Admin request to delete category ID: $categoryId" }
        categoryService.deleteCategory(categoryId)
        logger.info { "Admin successfully processed delete request for category ID: $categoryId" }
    }


}