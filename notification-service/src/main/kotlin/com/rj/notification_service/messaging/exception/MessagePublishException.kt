package com.rj.notification_service.messaging.exception

import java.lang.RuntimeException

class MessagePublishException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)