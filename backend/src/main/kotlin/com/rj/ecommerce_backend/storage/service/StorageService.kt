package com.rj.ecommerce_backend.storage.service

import org.springframework.core.io.Resource
import org.springframework.web.multipart.MultipartFile
import java.io.InputStream

interface StorageService {
    /** Stores a file and returns its unique, persistent identifier (e.g., filename or object key). */
    fun store(file: MultipartFile): String

    /** Stores a file from an InputStream and returns its identifier. */
    fun store(inputStream: InputStream, originalFileName: String): String

    /** Loads a file as a streamable resource. */
    fun loadAsResource(identifier: String): Resource

    /** Deletes a file using its identifier. */
    fun delete(identifier: String)

    /** Generates a publicly accessible URL for a given file identifier. */
    fun getPublicUrl(identifier: String): String
}