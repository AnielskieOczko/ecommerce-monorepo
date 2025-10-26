package com.rj.ecommerce_backend.security.repository

import com.rj.ecommerce_backend.security.domain.RefreshToken
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface RefreshTokenRepository : JpaRepository<RefreshToken, Long> {

    // Deletes all refresh tokens associated with a given user ID.
    // This is a bulk delete operation.
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.user.id = :userId")
    fun deleteByUserId(@Param("userId") userId: Long): Int

    @Query("SELECT rt FROM RefreshToken rt JOIN FETCH rt.user WHERE rt.token = :token")
    fun findByTokenWithUser(@Param("token") token: String): RefreshToken?

    // Derived query alternative (if RefreshToken.user is LAZY and you need it):
    // @EntityGraph(attributePaths = ["user"])
    // fun findByToken(token: String): RefreshToken?
}