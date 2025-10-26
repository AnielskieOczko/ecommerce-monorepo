package com.rj.ecommerce_backend.product.exception

class CategoryNotFoundException : RuntimeException {
    val missingIds: List<Long>
    
    // Original constructor for single ID
    constructor(id: Long) : super("Category not found with ID: $id") {
        this.missingIds = listOf(id)
    }
    
    // New constructor for multiple IDs
    constructor(ids: List<Long>) : super("Categories not found with IDs: $ids") {
        this.missingIds = ids.toList()
    }
    
    constructor(message: String) : super(message) {
        this.missingIds = emptyList()
    }
    
    constructor(message: String, ids: List<Long>) : super(message) {
        this.missingIds = ids.toList()
    }
}
