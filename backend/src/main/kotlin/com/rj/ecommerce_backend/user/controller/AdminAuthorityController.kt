package com.rj.ecommerce_backend.user.controller

import com.rj.ecommerce.api.shared.dto.user.common.AuthorityDetails
import com.rj.ecommerce_backend.user.service.AuthorityService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/authorities")
@PreAuthorize("hasRole('ADMIN')")
class AdminAuthorityController(
    private val authorityService: AuthorityService
) {

    companion object {
        private val logger = KotlinLogging.logger {} // Add logger
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllAuthorities(): ResponseEntity<Set<AuthorityDetails>> {
        logger.info { "Admin request to get all authorities." }
        val authorities = authorityService.getAllAuthoritiesDTO()
        return ResponseEntity.ok(authorities)
    }

    // may be needed not only by ADMIN (to consider remove pre-authorization)
    @GetMapping("/names")
    fun getAuthorityNamesForFilter(): ResponseEntity<Set<String>> {
        logger.info { "Admin request to get all authority names." }
        val authorityNames = authorityService.getAuthorityNames()
        return ResponseEntity.ok(authorityNames)
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createAuthority(@RequestBody authorityCreateRequest: AuthorityDetails): AuthorityDetails {
        logger.info { "Admin request to create new authority with name: ${authorityCreateRequest.name}" }

        val createdAuthority = authorityService.createAuthority(authorityCreateRequest)
        logger.info { "Admin successfully created authority. ID: ${createdAuthority.id}, Name: ${createdAuthority.name}" }
        return createdAuthority
    }

    // GET a specific Authority by ID (often useful)
    @GetMapping("/{authorityId}")
    fun getAuthorityById(@PathVariable authorityId: Long): ResponseEntity<AuthorityDetails> {
        logger.info { "Admin request to get authority by ID: $authorityId" }
        // AuthorityService would need: fun getAuthorityById(id: Long): AuthorityDTO? (or throws)
        val authority = authorityService.getAuthorityById(authorityId)
        // ?: throw AuthorityNotFoundException("Authority not found with ID: $authorityId")
        return ResponseEntity.ok(authority)
    }
}