package com.rj.ecommerce_backend.order.exception

class OrderServiceException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
