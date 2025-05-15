package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable

@Embeddable
@JvmRecord
data class Email(val value: String?) {
    companion object {
        fun of(email: String?): Email {
            return Email(email)
        }
    }
}
