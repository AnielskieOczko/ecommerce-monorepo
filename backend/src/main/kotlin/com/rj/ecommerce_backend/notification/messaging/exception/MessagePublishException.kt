package com.rj.ecommerce_backend.notification.messaging.exception

import java.lang.RuntimeException

class MessagePublishException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)