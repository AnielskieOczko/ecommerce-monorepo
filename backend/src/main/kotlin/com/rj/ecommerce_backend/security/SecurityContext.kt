package com.rj.ecommerce_backend.security

import com.rj.ecommerce_backend.user.domain.User

interface SecurityContext {
    fun ensureAccess(targetUserId: Long)
    fun getCurrentUser(): User
    fun isAdmin(): Boolean
    fun ensureAdmin()
}