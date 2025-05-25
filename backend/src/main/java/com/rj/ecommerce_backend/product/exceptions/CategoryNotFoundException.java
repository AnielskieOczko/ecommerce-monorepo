package com.rj.ecommerce_backend.product.exceptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CategoryNotFoundException extends RuntimeException {
    private List<Long> missingIds;

    // Original constructor for single ID
    public CategoryNotFoundException(Long id) {
        super("Category not found with ID: " + id);
        this.missingIds = Collections.singletonList(id);
    }

    // New constructor for multiple IDs
    public CategoryNotFoundException(List<Long> ids) {
        super("Categories not found with IDs: " + ids);
        this.missingIds = new ArrayList<>(ids);
    }

    public CategoryNotFoundException(String message) {
        super(message);
    }

    public CategoryNotFoundException(String message, List<Long> ids) {
        super(message);
        this.missingIds = new ArrayList<>(ids);
    }

    public List<Long> getMissingIds() {
        return missingIds != null ? Collections.unmodifiableList(missingIds) : Collections.emptyList();
    }
}
