package com.rj.ecommerce.api.shared.core

import jakarta.persistence.Embeddable

/**
 * Represents a postal address.
 *
 * @property street Street address line.
 * @property city City name.
 * @property zipCode Postal or Zip code.
 * @property country Country name.
 *
 * Requirements:
 * - All fields are required and cannot be blank
 */
@Embeddable
data class Address(
    val street: String? = null,
    val city: String? = null,
    val zipCode: ZipCode? = null,
    val country: String? = null
){

}
