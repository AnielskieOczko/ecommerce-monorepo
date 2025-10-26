package com.rj.ecommerce_backend.sorting

import com.rj.ecommerce_backend.user.exception.InvalidSortParameterException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Sort

import org.springframework.stereotype.Component

@Component
class SortValidator {
    companion object {
        private const val SORT_SEPARATOR: String = ":"
        private val VALID_SORT_DIRECTIONS: Set<String> = setOf("asc", "desc")
        private val logger = KotlinLogging.logger { }
    }

    fun <E> validateAndBuildSort(
        sortParamsString: String?,
        sortFieldEnumClass: Class<E>
    ): Sort where E : Enum<E>, E : SortableField {

        if (sortParamsString.isNullOrBlank()) {
            return Sort.unsorted()
        }

        logger.debug { "Validating and building sort for params: '$sortParamsString' with enum: ${sortFieldEnumClass.simpleName}" }

        try {
            val orders = sortParamsString.split(",")
                .mapNotNull { it.trim().takeIf { part -> part.isNotBlank() } }
                .map { sortPart -> createOrder(sortPart, sortFieldEnumClass) }
            return if (orders.isNotEmpty()) Sort.by(orders) else Sort.unsorted()
        } catch (e: InvalidSortParameterException) {
            logger.warn(e) { "Validation failed for sort parameter: '$sortParamsString'" }
            throw e
        } catch (e: Exception) {
            logger.error(e) { "Unexpected error during sort validation for param: '$sortParamsString'" }
            throw InvalidSortParameterException(
                "Unexpected error processing sort parameter: '$sortParamsString'. Cause: ${e.message}")
        }


    }

    private fun <E> createOrder(
        sortPart: String,
        sortFieldEnumClass: Class<E>
    ): Sort.Order where E : Enum<E>, E : SortableField {
        val parts: List<String> = sortPart.split(SORT_SEPARATOR)

        if (parts.size != 2) {
            throw InvalidSortParameterException("Invalid sort format. Expected 'field:direction'")
        }

        val fieldFromInput = parts[0].trim()
        val direction = parts[1].lowercase().trim()

        if (!VALID_SORT_DIRECTIONS.contains(direction)) {
            throw InvalidSortParameterException("Sort direction must be 'asc' or 'desc'")
        }

        val sortableFieldInstance: E = SortableField.fromString(fieldFromInput, sortFieldEnumClass)

        val databaseFieldName = sortableFieldInstance.fieldName // Access property directly
        logger.debug { "Validated sort field: '$fieldFromInput' maps to DB field: '$databaseFieldName', direction: '$direction'" }

        return Sort.Order(Sort.Direction.fromString(direction), databaseFieldName)

    }

}