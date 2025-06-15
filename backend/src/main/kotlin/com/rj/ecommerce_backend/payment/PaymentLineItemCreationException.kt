package com.rj.ecommerce_backend.payment

/**
 * Exception thrown when there's an issue creating payment line items from order data.
 */
class PaymentLineItemCreationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)

