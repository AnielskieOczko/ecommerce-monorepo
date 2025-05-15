package com.rj.ecommerce_backend.securityconfig

import com.rj.ecommerce_backend.user.domain.User

interface SecurityContext {
    fun ensureAccess(userId: Long)
    fun getCurrentUser(): User
    fun isAdmin(): Boolean
    fun ensureAdmin()
}