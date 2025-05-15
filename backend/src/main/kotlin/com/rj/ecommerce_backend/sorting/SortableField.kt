package com.rj.ecommerce_backend.sorting

import com.rj.ecommerce_backend.user.exceptions.InvalidSortParameterException

interface SortableField {

    val fieldName: String

    companion object {

        fun <E> fromString( // Generic type E for the specific enum
            field: String,
            enumClass: Class<E> // Expect the concrete enum class
        ): E where E : Enum<E>, E : SortableField { // E must be an enum and implement SortableField

            if (!enumClass.isEnum) {
                throw InvalidSortParameterException("Class ${enumClass.simpleName} is not an enum type.")
            }

            val enumConstants: Array<E> = enumClass.enumConstants
                ?: throw InvalidSortParameterException("Could not retrieve enum constants for ${enumClass.simpleName}.")
            return enumConstants.find { enumConstant ->
                enumConstant.name.equals(field, ignoreCase = true) ||
                        enumConstant.fieldName.equals(
                            field, ignoreCase = true
                        )
            } ?: throw InvalidSortParameterException(
                "Invalid sort field: '$field' for enum ${enumClass.simpleName}. " +
                        "Allowed values (by name or fieldName): " +
                        enumConstants.joinToString { "'${it.name}' or '${it.fieldName}'" }
            )
        }

    }


}
