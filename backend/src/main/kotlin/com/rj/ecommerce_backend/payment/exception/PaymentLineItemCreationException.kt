package com.rj.ecommerce_backend.payment.exception

/**
 * Exception thrown when there's an issue creating payment line items from order data.
 */
class PaymentLineItemCreationException(
    message: String,
    cause: Throwable? = null
) : RuntimeException(message, cause)