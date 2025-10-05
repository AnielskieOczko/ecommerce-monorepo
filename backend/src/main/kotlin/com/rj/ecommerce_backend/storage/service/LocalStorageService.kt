package com.rj.ecommerce_backend.storage.service

import com.rj.ecommerce_backend.product.exception.FileNotFoundInStorageException
import com.rj.ecommerce_backend.product.exception.FileStorageException
import com.rj.ecommerce_backend.storage.StorageProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.InputStream
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

@Service
@Profile("local-storage", "local")
class LocalStorageService(
    private val storageProperties: StorageProperties
) : StorageService {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val storageLocation: Path = Paths.get(storageProperties.location).toAbsolutePath()

    init {
        try {
            Files.createDirectories(storageLocation)
        } catch (e: IOException) {
            throw FileStorageException("Could not create storage directory at ${storageLocation.toAbsolutePath()}", e)
        }
    }

    @Retryable(
        value = [Exception::class],
        maxAttempts = 3,
        backoff = Backoff(1000)
    )
    override fun store(inputStream: InputStream, originalFileName: String): String {
        val uniqueFileName = generateUniqueFileName(originalFileName)
        val targetLocation = this.storageLocation.resolve(uniqueFileName)

        try {
            inputStream.use { Files.copy(it, targetLocation, StandardCopyOption.REPLACE_EXISTING) }
            logger.info { "Stored file locally: $uniqueFileName" }
            return uniqueFileName
        } catch (e: Exception) {
            logger.warn(e) { "Failed to store file $uniqueFileName. Retrying..." }
            throw e
        }
    }

    override fun store(file: MultipartFile): String {
        // Delegate to the more generic InputStream method to avoid code duplication
        return store(file.inputStream, file.originalFilename ?: "unknown")
    }

    // 2. RECOVER FIX: This recover method handles failures from store(MultipartFile)
    @Recover
    fun recoverFromStorageFailure(e: Exception, file: MultipartFile): String {
        val originalFilename = file.originalFilename ?: "unknown"
        logger.error(e) { "Could not store file $originalFilename after multiple attempts." }
        throw FileStorageException("Could not store file $originalFilename after all retries", e)
    }

    // 2. RECOVER FIX: This new recover method handles failures from store(InputStream, String)
    @Recover
    fun recoverFromStorageFailure(e: Exception, inputStream: InputStream, originalFileName: String): String {
        logger.error(e) { "Could not store file $originalFileName from input stream after multiple attempts." }
        throw FileStorageException("Could not store file $originalFileName after all retries", e)
    }


    override fun loadAsResource(identifier: String): Resource {
        logger.debug { "Attempting to load file as resource: '$identifier'" }
        try {
            val filePath = storageLocation.resolve(identifier).normalize()
            val resource = UrlResource(filePath.toUri())

            if (resource.exists() && resource.isReadable) {
                logger.debug { "File resource loaded successfully: '$identifier'" }
                return resource
            } else {
                logger.warn { "File not found or not readable: '$identifier' at path '$filePath'" }
                // FIXED: Throw the new, more specific exception for a 404 response.
                throw FileNotFoundInStorageException("File not found or not readable: $identifier")
            }
        } catch (ex: MalformedURLException) {
            logger.error(ex) { "Malformed URL for file identifier: '$identifier'" }
            throw FileStorageException("File path is invalid (malformed URL): $identifier", ex)
        }
    }

    override fun delete(identifier: String) {
        try {
            val filePath = storageLocation.resolve(identifier).normalize()
            if (Files.exists(filePath)) {
                Files.delete(filePath)
                logger.info { "Successfully deleted physical file: '$identifier'" }
            } else {
                logger.warn { "Attempted to delete non-existent physical file: '$identifier'" }
            }
        } catch (e: IOException) {
            logger.error(e) { "Error deleting physical file: '$identifier'" }
            // Re-throw as our custom exception so the caller can handle it if needed.
            throw FileStorageException("Could not delete file: $identifier", e)
        }
    }

    override fun getPublicUrl(identifier: String): String {
        return "${storageProperties.baseUrl}/api/v1/public/products/images/$identifier"
    }

    private fun generateUniqueFileName(originalFileName: String?): String {
        requireNotNull(originalFileName) { "Original filename cannot be null." }

        val fileExtension = StringUtils.getFilenameExtension(originalFileName)
        val baseName = StringUtils.stripFilenameExtension(originalFileName)
            .replace(Regex("[^a-zA-Z0-9.-]"), "_")
            .take(50)

        val uniquePart = UUID.randomUUID().toString().substring(0, 8)
        return "$baseName-$uniquePart${if (!fileExtension.isNullOrBlank()) ".$fileExtension" else ""}"
    }
}