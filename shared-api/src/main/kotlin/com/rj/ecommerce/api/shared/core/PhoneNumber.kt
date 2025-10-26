package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable

/**
 * Represents a phone number.
 *
 * @property value The phone number value as a string.
 *
 * Requirements:
 * - value must be a valid phone number format
 */
@Embeddable
data class PhoneNumber(
    val value: String
)
