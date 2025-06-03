package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable
import jakarta.validation.constraints.NotEmpty

@Embeddable
@JvmRecord
data class Password(val value: @NotEmpty String) {
    companion object {
        fun of(password: String): Password {
            validatePassword(password)
            return Password(password)
        }

        private fun validatePassword(password: String) {
            // Add your password validation logic here
            require(!(password.isEmpty() || password.length < 8)) { "Password must be at least 8 characters long" }
        }
    }
}
