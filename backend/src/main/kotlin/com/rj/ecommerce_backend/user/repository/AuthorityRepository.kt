package com.rj.ecommerce_backend.user.repository

import com.rj.ecommerce_backend.user.domain.Authority
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository // Spring annotation remains
interface AuthorityRepository : JpaRepository<Authority, Long> {

    @Transactional(readOnly = true)
    fun findByName(name: String): Authority?

    @Transactional(readOnly = true)
    fun findByNameIn(names: Set<String>): Set<Authority>
}