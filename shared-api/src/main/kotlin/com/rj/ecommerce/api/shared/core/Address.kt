package com.rj.ecommerce.api.shared.core

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
data class Address(
    val street: String,
    val city: String,
    val zipCode: String,
    val country: String
)
