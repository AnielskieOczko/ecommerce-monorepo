package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.user.AuthorityDTO
import com.rj.ecommerce_backend.user.domain.Authority
import com.rj.ecommerce_backend.user.exceptions.AuthorityAlreadyExistsException
import com.rj.ecommerce_backend.user.repository.AuthorityRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthorityServiceImpl(
    private val authorityRepository: AuthorityRepository
) : AuthorityService {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @Transactional
    override fun addNewAuthority(authority: Authority) {

        if (authorityRepository.findByName(authority.name) != null) {
            logger.warn { "Attempt to add duplicate authority name: ${authority.name}" }
            throw AuthorityAlreadyExistsException("Authority with name '${authority.name}' already exists.")
        }

        val savedAuthority = authorityRepository.save(authority)
        logger.info { "New authority added ${savedAuthority.name}" }
    }

    @Transactional(readOnly = true)
    override fun getAllAuthoritiesDTO(): Set<AuthorityDTO> {
        logger.debug { "Fetching all authorities and mapping to DTOs." }
        val authorities: Set<Authority> = authorityRepository.findAll().toSet()

        return authorities.map { authority ->
            AuthorityDTO(
                id = authority.id,
                name = authority.name
            )
        }.toSet()
    }

    @Transactional(readOnly = true)
    override fun getAuthorityNames(): Set<String> {
        logger.debug { "Fetching all authority names." }
        val authorities: Set<Authority> = authorityRepository.findAll().toSet()

        return authorities.map { authority ->
            authority.name
        }.toSet()
    }

    @Transactional(readOnly = true)
    override fun findAuthorityByRoleName(roleName: String): Authority? {
        logger.debug { "Finding authority by role name: $roleName" }
        return authorityRepository.findByName(roleName)
    }

}