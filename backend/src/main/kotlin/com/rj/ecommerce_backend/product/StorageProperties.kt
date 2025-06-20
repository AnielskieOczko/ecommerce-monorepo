package com.rj.ecommerce_backend.product

import com.rj.ecommerce_backend.product.exception.FileStorageException
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.validation.annotation.Validated
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths


@ConfigurationProperties(prefix = "storage")
@Validated
class StorageProperties {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @field:NotBlank(message = "Storage location must be configured and not blank.")
    lateinit var location: String

    @field:NotBlank(message = "Base URL for images cannot be blank.")
    var baseUrl: String = "/images"

    var cleanupSchedule: String? = null

    var cleanupEnabled: Boolean = true

    @field:Min(1, message = "Cleanup threshold must be at least 1 day.")
    var cleanupThresholdDays: Int = 7

    var tempDir: String? = null

    @PostConstruct
    fun init() {
        logger.info { "Initializing storage properties..." }
        try {
            val uploadPath = Paths.get(location)
            Files.createDirectories(uploadPath)
            logger.info { "Storage location initialized and verified/created at: ${uploadPath.toAbsolutePath()}" }

            tempDir?.let {
                if (it.isNotBlank()) {
                    val tempPath = Paths.get(it)
                    Files.createDirectories(tempPath)
                    logger.info { "Temporary directory initialized and verified/created at: ${tempPath.toAbsolutePath()}" }
                } else {
                    logger.warn { "Configured tempDir is blank, not creating temporary directory." }
                }
            }
        } catch (ioe: IOException) {
            logger.error(ioe) {
                "Could not initialize storage location: $location" + (tempDir?.let { " or temp directory: $it" } ?: "")
            }
            throw FileStorageException(
                "Could not initialize storage directory. Please check configuration and permissions.",
                ioe
            )
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during storage properties initialization." }
            throw FileStorageException("Unexpected error initializing storage.", e)
        }
    }

}