package com.rj.ecommerce_backend.product.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus


@ResponseStatus(HttpStatus.CONFLICT) // 409 Conflict is a good status for this
class ProductInUseException(message: String) : RuntimeException(message)