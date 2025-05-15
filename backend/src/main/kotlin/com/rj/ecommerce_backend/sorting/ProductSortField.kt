package com.rj.ecommerce_backend.sorting

enum class ProductSortField(
    override val fieldName: String

) : SortableField {
    ID("id"),
    QUANTITY("quantityInStock.value"),
    PRICE("unitPrice.amount"),
    NAME("name"),
    CREATED_AT("createdAt")
}