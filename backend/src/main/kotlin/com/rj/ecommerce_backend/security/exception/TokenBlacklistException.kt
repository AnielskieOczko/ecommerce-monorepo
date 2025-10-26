package com.rj.ecommerce_backend.security.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
class TokenBlacklistException : RuntimeException {
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(message: String?) : super(message)
}


