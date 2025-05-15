package com.rj.ecommerce_backend.sorting

enum class OrderSortField(
    override val fieldName: String
) : SortableField {
    ID("id"),
    TOTAL("totalPrice"),
    CREATED_AT("createdAt"),
    STATUS("orderStatus"),
}