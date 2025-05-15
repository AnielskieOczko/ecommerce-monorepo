package com.rj.ecommerce.api.shared.dto.customer

import com.rj.ecommerce.api.shared.core.PhoneNumber

/**
 * Basic information about a customer/user.
 *
 * @property id User ID (Using String for cross-service consistency).
 * @property firstName First name of the customer.
 * @property lastName Last name of the customer.
 * @property email Email address of the customer.
 * @property phoneNumber Phone number of the customer.
 *
 * Requirements:
 * - id and email are required
 * - firstName, lastName, and phoneNumber are optional
 */
data class CustomerInfo(
    val id: String,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String,
    val phoneNumber: PhoneNumber? = null
)
