package com.rj.ecommerce_backend.sorting;

enum class UserSortField(
    override val fieldName: String
) : SortableField {

    ID("id"),                           // Sorts by the 'id' property of User entity
    EMAIL("email.value"),               // Sorts by User.email.value (assuming Email VO with 'value' field)
    FIRST_NAME("firstName"),            // Sorts by User.firstName
    LAST_NAME("lastName"),              // Sorts by User.lastName
    IS_ACTIVE("isActive"),              // Sorts by User.isActive

    // AUTHORITIES("authorities")      // Sorting by a collection itself is usually not what you want.
    // If you wanted to sort by, say, the *count* of authorities or
    // the name of the *first* authority, that would require
    // a more complex query or is not suitable for simple property path sorting.
    // For now, let's assume direct properties or simple paths.
    // If sorting by a property of an Authority (e.g., Authority name for users
    // associated with a specific authority), it would require a join and
    // is often handled by a specific query rather than a generic sort field.
    CREATED_AT("createdAt");            // Sorts by User.createdAt

}