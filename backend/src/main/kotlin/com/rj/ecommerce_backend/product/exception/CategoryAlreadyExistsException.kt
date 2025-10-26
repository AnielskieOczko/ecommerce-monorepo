package com.rj.ecommerce_backend.product.exception

class CategoryAlreadyExistsException : RuntimeException {
    constructor(name: String) : super("Category with name '$name' already exists.")
    constructor(id: Long) : super("Category with id '$id' already exists.")
}
