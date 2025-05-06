package com.rj.ecommerce.api.shared.enums

/**
 * Represents the method used for shipping.
 *
 * Values:
 * - INPOST: Shipping via InPost (parcel lockers)
 * - DHL: Shipping via DHL courier
 * - STANDARD: Standard shipping method
 * - EXPRESS: Express shipping method
 */
enum class ShippingMethod {
    INPOST,
    DHL,
    STANDARD,
    EXPRESS
}
