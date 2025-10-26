package com.rj.ecommerce_backend.user.exception

class AuthorityNotFoundException : RuntimeException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}
