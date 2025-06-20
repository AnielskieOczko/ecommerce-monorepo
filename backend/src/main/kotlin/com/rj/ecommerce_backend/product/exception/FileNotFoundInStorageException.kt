package com.rj.ecommerce_backend.product.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

// This new exception is specifically for 404 errors.
@ResponseStatus(HttpStatus.NOT_FOUND)
class FileNotFoundInStorageException : FileStorageException {
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
}