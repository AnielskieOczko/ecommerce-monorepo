package com.rj.ecommerce_backend.user.search

import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce_backend.user.domain.Authority
import com.rj.ecommerce_backend.user.domain.User
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.criteria.Join
import org.springframework.data.jpa.domain.Specification

private val logger = KotlinLogging.logger { }

object UserSpecifications {

    fun withSearchCriteria(search: String?): Specification<User>? {
        if (search.isNullOrBlank()) return null

        return Specification { root, _, cb ->
            val searchLower = "%${search.lowercase()}%"
            logger.debug { "Applying user search criteria: '$search'" }

            cb.or(
                cb.like(cb.lower(root.get("firstName")), searchLower),
                cb.like(cb.lower(root.get("lastName")), searchLower),
                cb.like(cb.lower(root.get<Email>("email").get("value")), searchLower)
            )
        }
    }

    fun withActiveStatus(isActive: Boolean?): Specification<User>? {
        if (isActive == null) return null
        logger.debug { "Filtering user by active status: '$isActive'" }

        return Specification { root, _, cb ->
            cb.equal(root.get<Boolean>("isActive"), isActive)
        }
    }

    fun withRole(role: String?): Specification<User>? {
        if (role.isNullOrBlank()) return null
        logger.debug { "Filtering users by role: '$role'" } // Added logging

        return Specification { root, _, cb -> // query unused, can be _
            // Join the 'authorities' collection (which is Set<Authority> on User entity)
            val authoritiesJoin: Join<User, Authority> = root.join("authorities")
            // Now filter on the 'name' attribute of the joined Authority entities
            cb.equal(cb.lower(authoritiesJoin.get("name")), role.lowercase()) // Compare with lowercase role
        }
    }


}