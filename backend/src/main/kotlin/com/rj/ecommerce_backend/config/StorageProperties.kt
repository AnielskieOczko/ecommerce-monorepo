package com.rj.ecommerce_backend.config

import org.springframework.boot.context.properties.ConfigurationProperties
/**
 * A type-safe configuration properties class for mapping all properties
 * under the 'app.storage' key in application.yml.
 */
@ConfigurationProperties(prefix = "app.storage")
data class StorageProperties(
    val location: String,
    val baseUrl: String,
    val cleanupSchedule: String,
    val secretSalt: String
)