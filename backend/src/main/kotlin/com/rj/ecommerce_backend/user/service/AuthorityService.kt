package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.user.AuthorityDTO
import com.rj.ecommerce.api.shared.dto.user.AuthorityRequestDTO
import com.rj.ecommerce_backend.user.domain.Authority

interface AuthorityService {

    fun createAuthority(authorityCreateRequestDTO: AuthorityRequestDTO): AuthorityDTO
    fun getAllAuthoritiesDTO(): Set<AuthorityDTO>
    fun getAuthorityNames(): Set<String>
    fun findAuthorityByRoleName(roleName: String): Authority?
    fun getAuthorityById(authorityId: Long): AuthorityDTO?
}