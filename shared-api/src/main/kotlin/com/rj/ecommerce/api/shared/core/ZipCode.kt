package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable

// validation
@Embeddable
@JvmRecord
data class ZipCode(val value: String?)
