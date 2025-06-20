package com.rj.ecommerce_backend.order.exception

class OrderNotFoundException : RuntimeException {
    constructor(orderId: Long) : super("Order not found with ID: $orderId")
    constructor(message: String) : super(message)
}
