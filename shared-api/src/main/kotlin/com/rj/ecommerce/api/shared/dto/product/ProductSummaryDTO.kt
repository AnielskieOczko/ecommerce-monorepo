package com.rj.ecommerce.api.shared.dto.product

import com.rj.ecommerce.api.shared.core.Money

/**
 * Core identifying information for a product. Used for composition, not directly generated unless needed.
 *
 * @property id Product ID (Using String for cross-service consistency).
 * @property name Product name.
 * @property unitPrice Price per unit of the product.
 *
 * Requirements:
 * - id, name, and unitPrice are required
 * - sku is optional
 */
data class ProductSummaryDTO(
    val id: Long,
    val name: String? = null,
    val unitPrice: Money? = null
)
