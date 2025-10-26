package com.rj.ecommerce_backend.messaging.common.exception

class MessageDispatchException(message: String, cause: Throwable): RuntimeException(message, cause) {
}