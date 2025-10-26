package com.rj.ecommerce_backend.messaging.common.exception

class MessagePublishException(message: String, cause: Throwable) : RuntimeException(message, cause)