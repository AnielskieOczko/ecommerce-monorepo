package com.rj.ecommerce_backend.user.controller

import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import com.rj.ecommerce_backend.sorting.SortValidator
import com.rj.ecommerce_backend.sorting.UserSortField
import com.rj.ecommerce_backend.user.domain.Authority
import com.rj.ecommerce_backend.user.search.UserSearchCriteria
import com.rj.ecommerce_backend.user.service.AdminService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin/users")
class AdminController(
    private val adminService: AdminService,
    private val sortValidator: SortValidator
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun getUserById(@PathVariable userId: Long): UserInfoDTO {
        return adminService.getUserById(userId)
    }

    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id:asc") sort: String,
        // Spring will automatically bind request parameters to fields of UserSearchCriteria
        // if the parameter names in the HTTP request match the field names in UserSearchCriteria.
        userSearchCriteria: UserSearchCriteria
    ): Page<UserInfoDTO> {
        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort, UserSortField::)
        val pageable: Pageable = PageRequest.of(page, size, sortValidator)
    }
}