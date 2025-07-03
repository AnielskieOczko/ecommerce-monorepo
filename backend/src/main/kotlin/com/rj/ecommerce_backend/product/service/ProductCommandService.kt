package com.rj.ecommerce_backend.product.service

interface ProductCommandService {

    fun reduceProductQuantity(productId: Long, quantityInStockToReduce: Int)

}