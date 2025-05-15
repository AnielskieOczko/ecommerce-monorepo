package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.user.AuthorityCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.user.AuthorityDTO
import com.rj.ecommerce_backend.user.domain.Authority
import com.rj.ecommerce_backend.user.exceptions.AuthorityAlreadyExistsException
import com.rj.ecommerce_backend.user.exceptions.AuthorityNotFoundException
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
    override fun createAuthority(authorityCreateRequestDTO: AuthorityCreateRequestDTO): AuthorityDTO {

        if (authorityRepository.findByName(authorityCreateRequestDTO.name) != null) {
            logger.warn { "Attempt to add duplicate authority name: ${authorityCreateRequestDTO.name}" }
            throw AuthorityAlreadyExistsException("Authority with name '${authorityCreateRequestDTO.name}' already exists.")
        }

        val authority: Authority = Authority(
            name = authorityCreateRequestDTO.name
        )

        val savedAuthority = authorityRepository.save(authority)
        logger.info { "New authority added ${savedAuthority.name}" }

        return AuthorityDTO(savedAuthority.id, savedAuthority.name)
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

    override fun getAuthorityById(authorityId: Long): AuthorityDTO? {

        val authority = authorityRepository.findById(authorityId).orElseThrow {
            throw AuthorityNotFoundException("Authority not found.")
        }
        val authorityDTO = AuthorityDTO(
            id = authority.id,
            name = authority.name
        )

        return authorityDTO
    }

}