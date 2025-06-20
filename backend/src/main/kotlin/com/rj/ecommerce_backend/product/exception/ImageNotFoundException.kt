package com.rj.ecommerce_backend.product.exception

open class ImageNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
