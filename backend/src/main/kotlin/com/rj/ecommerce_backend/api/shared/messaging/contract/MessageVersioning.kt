package com.rj.ecommerce_backend.api.shared.messaging.contract

/**
 * A singleton object for handling message versioning contracts.
 * This helps with maintaining backward compatibility and managing API changes,
 * particularly for asynchronous messages.
 */
object MessageVersioning {

    // Use 'const val' for compile-time constants
    const val CURRENT_VERSION = "1.0"
    const val MINIMUM_SUPPORTED_VERSION = "1.0"

    /**
     * Checks if the provided version string is supported by the current implementation.
     *
     * @param version The version string to check (e.g., "1.0").
     * @return true if the version is supported, false otherwise.
     */
    fun isSupported(version: String?): Boolean {
        if (version.isNullOrEmpty()) {
            return false
        }

        val versionNum = version.toDoubleOrNull() ?: return false
        val minVersion = MINIMUM_SUPPORTED_VERSION.toDouble()
        val currentVersion = CURRENT_VERSION.toDouble()

        return versionNum >= minVersion && versionNum <= currentVersion
    }
}