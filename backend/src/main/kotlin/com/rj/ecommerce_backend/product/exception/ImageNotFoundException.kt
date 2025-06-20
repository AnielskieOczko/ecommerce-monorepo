package com.rj.ecommerce_backend.product.exception

class ImageNotFoundException(imageId: Long) : RuntimeException("Image not found with id: $imageId")
