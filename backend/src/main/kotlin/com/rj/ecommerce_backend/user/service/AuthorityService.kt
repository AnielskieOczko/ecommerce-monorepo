package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce_backend.api.shared.dto.user.common.AuthorityDetails
import com.rj.ecommerce_backend.user.domain.Authority

interface AuthorityService {

    fun createAuthority(authorityCreateRequestDTO: AuthorityDetails): AuthorityDetails
    fun getAllAuthoritiesDTO(): Set<AuthorityDetails>
    fun getAuthorityNames(): Set<String>
    fun findAuthorityByRoleName(roleName: String): Authority?
    fun getAuthorityById(authorityId: Long): AuthorityDetails?
}