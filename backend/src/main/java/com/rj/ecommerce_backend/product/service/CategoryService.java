package com.rj.ecommerce_backend.product.service;

import com.rj.ecommerce_backend.product.filters.CategoryCreateDTO;
import com.rj.ecommerce_backend.product.filters.CategoryResponseDTO;
import com.rj.ecommerce_backend.product.search.CategorySearchCriteria;
import com.rj.ecommerce_backend.product.filters.CategoryUpdateDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    CategoryResponseDTO createCategory(CategoryCreateDTO categoryDTO);
    Optional<CategoryResponseDTO> getCategoryById(Long id);
    Page<CategoryResponseDTO> getAllCategories(Pageable pageable, CategorySearchCriteria criteria);
    List<String> getCategoryNames();
    CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO updatedCategoryDTO);
    void deleteCategory(Long id);

}
