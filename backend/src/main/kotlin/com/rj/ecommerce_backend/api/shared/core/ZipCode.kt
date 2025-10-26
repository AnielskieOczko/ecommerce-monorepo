package com.rj.ecommerce_backend.api.shared.core

import jakarta.persistence.Embeddable

// validation
@Embeddable
@JvmRecord
data class ZipCode(val value: String?)
