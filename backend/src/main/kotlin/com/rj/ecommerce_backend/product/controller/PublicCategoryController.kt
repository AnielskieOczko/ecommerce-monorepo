package com.rj.ecommerce_backend.product.controller

import com.rj.ecommerce.api.shared.dto.product.common.CategoryDetails
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria
import com.rj.ecommerce_backend.product.service.category.CategoryService
import com.rj.ecommerce_backend.sorting.CategorySortField
import com.rj.ecommerce_backend.sorting.SortValidator
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("api/v1/public/categories")
class PublicCategoryController(
    private val categoryService: CategoryService,
    private val sortValidator: SortValidator
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @GetMapping
    fun getAllCategories(
        categorySearchCriteria: CategorySearchCriteria,
        @RequestParam(defaultValue = "0", required = false) page: Int,
        @RequestParam(defaultValue = "10", required = false) size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?
    ): ResponseEntity<Page<CategoryDetails>> {
        logger.info {
            "Public request to get all categories. Criteria: $categorySearchCriteria, " +
                    "Page: $page, Size: $size, Sort: '$sort'"
        }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort,
            CategorySortField::class.java
        )

        val pageable = PageRequest.of(page, size, validatedSort)

        val categoriesPage = categoryService.getAllCategories(pageable, categorySearchCriteria)
        logger.debug { "Retrieved ${categoriesPage.numberOfElements} categories on page $page for public view." }
        return ResponseEntity.ok(categoriesPage)
    }


    @GetMapping("/{categoryId}")
    fun getCategoryById(@PathVariable categoryId: Long): ResponseEntity<CategoryDetails> {
        logger.info { "Public request to get category by ID: $categoryId" }
        val categoryDto = categoryService.getCategoryById(categoryId)
        return ResponseEntity.ok(categoryDto)
    }

    @GetMapping("/names")
    fun getCategoryNames(): ResponseEntity<List<String>> {
        logger.info { "Public request to get all category names." }

        return ResponseEntity.ok(categoryService.getCategoryNames())
    }


}