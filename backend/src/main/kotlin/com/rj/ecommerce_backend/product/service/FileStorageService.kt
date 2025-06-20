package com.rj.ecommerce_backend.product.service

import com.rj.ecommerce_backend.product.StorageProperties
import com.rj.ecommerce_backend.product.domain.Image
import com.rj.ecommerce_backend.product.domain.Product
import com.rj.ecommerce_backend.product.exception.FileNotFoundInStorageException
import com.rj.ecommerce_backend.product.exception.FileStorageException
import com.rj.ecommerce_backend.product.repository.ImageRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.FileNotFoundException
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.util.UUID

// Logger at the file level
private val logger = KotlinLogging.logger {}

@Service
class FileStorageService( // Constructor injection
    private val storageProperties: StorageProperties,
    private val imageRepository: ImageRepository
) {

    // Base storage path, initialized once
    private val fileStorageLocation: Path by lazy {
        Paths.get(storageProperties.location).toAbsolutePath().normalize()
            .also { Files.createDirectories(it) } // Ensure directory exists
    }

    /**
     * Stores a file, creates an Image entity, and associates it with a Product.
     * The Image entity is saved.
     *
     * @param file The MultipartFile to store.
     * @param altText Alternative text for the image.
     * @param product The Product to associate this image with.
     * @return The saved Image entity.
     * @throws FileStorageException if the file cannot be stored.
     */
    @Transactional // This method now involves a database write (imageRepository.save)
    fun storeFile(file: MultipartFile, altText: String, product: Product): Image {
        val originalFilename = StringUtils.cleanPath(file.originalFilename ?: "unknown_file")
        logger.debug { "Attempting to store file: '$originalFilename' with alt text: '$altText' for product ID: ${product.id}" }

        if (file.isEmpty) {
            throw FileStorageException("Failed to store empty file '$originalFilename'.")
        }

        try {
            val uniqueFileName = generateUniqueFileName(originalFilename)
            val targetLocation = fileStorageLocation.resolve(uniqueFileName)

            storeFileWithRetry(file, targetLocation)

            // Path stored in DB is just the unique filename, relative to storageLocation
            val dbPath = uniqueFileName

            // Create and save image entity
            val imageEntity = Image( // Assuming Image constructor: path, altText, mimeType, product
                path = dbPath,
                altText = altText.ifBlank { "Product image for ${product.name?.value ?: "product"}" },
                mimeType = file.contentType ?: "application/octet-stream",
                product = product
            )

            imageEntity.product = product
            val savedImage = imageRepository.save(imageEntity)


            logger.info {
                "Successfully stored file: '$uniqueFileName' as Image ID: ${savedImage.id} " +
                        "and associated with product ID: ${product.id}"
            }
            return savedImage

        } catch (ioe: IOException) {
            logger.error(ioe) { "Failed to store file '$originalFilename' due to IOException." }
            throw FileStorageException("Could not store file '$originalFilename'. Please try again!", ioe)
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error storing file '$originalFilename'." }
            throw FileStorageException("An unexpected error occurred while storing file '$originalFilename'.", e)
        }
    }

    private fun generateUniqueFileName(originalFileName: String): String {
        val fileExtension = StringUtils.getFilenameExtension(originalFileName)
        val baseName = StringUtils.stripFilenameExtension(originalFileName)
            .replace(Regex("[^a-zA-Z0-9.-]"), "_") // Sanitize base name
            .take(50) // Limit base name length

        val uniquePart = UUID.randomUUID().toString().substring(0, 8)
        return "$baseName-$uniquePart${if (!fileExtension.isNullOrBlank()) ".$fileExtension" else ""}"
    }

    // getTargetLocation is now effectively inlined by using fileStorageLocation.resolve()

    private fun storeFileWithRetry(file: MultipartFile, targetLocation: Path, maxRetries: Int = 3, delayMillis: Long = 1000) {
        var attempts = 0
        var lastException: IOException? = null
        while (attempts < maxRetries) {
            try {
                file.inputStream.use { inputStream -> // Use- H-resource for InputStream
                    Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)
                }
                logger.debug { "File copied successfully to $targetLocation on attempt ${attempts + 1}" }
                return // Success
            } catch (e: IOException) {
                lastException = e
                attempts++
                if (attempts < maxRetries) {
                    logger.warn(e) {
                        "Retry ${attempts} of $maxRetries for storing file ${targetLocation.fileName}. Waiting ${delayMillis}ms."
                    }
                    try {
                        Thread.sleep(delayMillis)
                    } catch (ie: InterruptedException) {
                        Thread.currentThread().interrupt()
                        throw FileStorageException("File storage retry interrupted for ${targetLocation.fileName}", ie)
                    }
                }
            }
        }
        // If all retries fail, throw the last encountered exception
        throw FileStorageException(
            "Could not store file ${targetLocation.fileName} after $maxRetries attempts. Last error: ${lastException?.message}",
            lastException
        )
    }

    fun loadFileAsResource(fileName: String): Resource {
        logger.debug { "Attempting to load file as resource: '$fileName'" }
        try {
            val filePath = fileStorageLocation.resolve(fileName).normalize()
            val resource = UrlResource(filePath.toUri())

            if (resource.exists() && resource.isReadable) {
                logger.debug { "File resource loaded successfully: '$fileName'" }
                return resource
            } else {
                logger.warn { "File not found or not readable: '$fileName' at path '$filePath'" }
                // FIXED: Throw the new, more specific exception for a 404 response.
                throw FileNotFoundInStorageException("File not found or not readable: $fileName")
            }
        } catch (ex: MalformedURLException) {
            logger.error(ex) { "Malformed URL for file: '$fileName'" }
            // This is a server configuration issue, so the base 500 exception is still appropriate.
            throw FileStorageException("File path is invalid (malformed URL): $fileName", ex)
        } catch (fnf: FileNotFoundException){
            logger.warn(fnf) { "File not found during resource creation: '$fileName'" }
            // FIXED: Throw the new, more specific exception for a 404 response.
            throw FileNotFoundInStorageException("File not found: $fileName", fnf)
        }
    }

    @Transactional
    fun deleteImage(image: Image) {
        val fileName = image.path

        logger.debug { "Attempting to delete image entity ID: ${image.id} and physical file: '$fileName'" }

        try {
            val filePath = fileStorageLocation.resolve(fileName).normalize()

            if (Files.exists(filePath)) {
                try {
                    Files.delete(filePath)
                    logger.info { "Successfully deleted physical file: '$fileName' at path '$filePath'" }
                } catch (ioe: IOException) {
                    logger.error(ioe) { "Could not delete physical file: '$fileName' for image ID: ${image.id}" }
                    throw FileStorageException("Could not delete physical file for image: '${image.id}', path: '$fileName'", ioe)
                }
            } else {
                logger.warn { "Physical file not found for deletion: '$fileName' for image ID: ${image.id}. Entity will still be deleted." }
            }

            // Delete the image entity from the database
            imageRepository.delete(image)
            logger.info { "Successfully deleted image entity ID: ${image.id}" }

        } catch (e: Exception) {
            logger.error(e) { "Unexpected error deleting image ID: ${image.id}, file: '$fileName'" }
            throw FileStorageException("An unexpected error occurred while deleting image '${image.id}', file: '$fileName'", e)
        }
    }
}