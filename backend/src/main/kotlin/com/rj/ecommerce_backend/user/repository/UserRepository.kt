package com.rj.ecommerce_backend.user.repository

import com.rj.ecommerce_backend.api.shared.core.Email
import com.rj.ecommerce_backend.user.domain.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface UserRepository : JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Transactional(readOnly = true)
    fun findUserByEmail(email: Email): User?

    @Transactional(readOnly = true)
    fun findUserByFirstName(firstName: String): User?// Standard Spring Data query method

    @Transactional(readOnly = true)
    @Query("SELECT u FROM User u JOIN u.authorities a WHERE a.name = :roleName ORDER BY u.email ASC")
    fun findByAuthorities_Name(@Param("roleName") roleName: String, pageable: Pageable): Page<User>

    @Transactional(readOnly = true)
    fun existsByEmail(email: Email): Boolean

    @Transactional
    fun existsByEmailAndIdNot(email: Email, id: Long): Boolean
}