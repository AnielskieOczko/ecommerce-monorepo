package com.rj.ecommerce_backend.security.exception

class TokenRefreshException : RuntimeException {
    constructor(message: String?) : super(message)

    constructor(message: String?, cause: Throwable?) : super(message, cause)
}
