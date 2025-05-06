package com.rj.ecommerce.api.shared.enums

/**
 * Identifier for the email template to use.
 *
 * Values:
 * - ORDER_CONFIRMATION: Email sent to confirm an order has been placed
 * - WELCOME_EMAIL: Email sent to welcome a new user
 * - PAYMENT_SUCCESS: Email sent when payment is successful
 * - PAYMENT_FAILURE: Email sent when payment fails
 * - ORDER_STATUS_UPDATE: Email sent when order status changes
 */
enum class EmailTemplate {
    ORDER_CONFIRMATION,
    WELCOME_EMAIL,
    PAYMENT_SUCCESS,
    PAYMENT_FAILURE,
    ORDER_STATUS_UPDATE
}
