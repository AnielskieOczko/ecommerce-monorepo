package com.rj.ecommerce_backend.product.exception

class ProductNotFoundException(id: Long) : RuntimeException("Product not found with ID: $id")
