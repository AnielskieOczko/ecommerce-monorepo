package com.rj.ecommerce_backend.sorting

enum class CategorySortField(
    override val fieldName: String
) : SortableField {
    ID("id"),
    NAME("name")
}