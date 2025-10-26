package com.rj.ecommerce_backend.user.search

import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.jpa.domain.Specification

data class UserSearchCriteria(
    val search: String,
    val isActive: Boolean,
    val authority: String
) {
    companion object {
        private val logger = KotlinLogging.logger {  }
    }

    fun toSpecification(): Specification<User> {
        return Specification
            .where(UserSpecifications.withSearchCriteria(search))
            .and(UserSpecifications.withActiveStatus(isActive))
            .and(UserSpecifications.withRole(authority))
    }
}