package com.rj.ecommerce_backend.user.controller

import com.rj.ecommerce_backend.api.shared.dto.user.request.AdminChangeUserAuthorityRequest
import com.rj.ecommerce_backend.api.shared.dto.user.request.AdminUpdateUserRequest
import com.rj.ecommerce_backend.api.shared.dto.user.request.ChangeAccountStatusRequest
import com.rj.ecommerce_backend.api.shared.dto.user.request.UserCreateRequest
import com.rj.ecommerce_backend.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.sorting.SortValidator
import com.rj.ecommerce_backend.sorting.UserSortField
import com.rj.ecommerce_backend.user.search.UserSearchCriteria
import com.rj.ecommerce_backend.user.service.AdminService
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
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
    fun getUserById(@PathVariable userId: Long): UserResponse {
        return adminService.getUserById(userId)
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllUsers(
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(defaultValue = "id:asc", required = false) sort: String?,
        // Spring will automatically bind request parameters to fields of UserSearchCriteria
        // if the parameter names in the HTTP request match the field names in UserSearchCriteria.
        userSearchCriteria: UserSearchCriteria
    ): ResponseEntity<Page<UserResponse>> {

        logger.info { "Admin request to get all users. Page: $page, Size: $size, Sort: '$sort', Criteria: $userSearchCriteria" }

        val validatedSort: Sort = sortValidator.validateAndBuildSort(
            sort,
            UserSortField::class.java
        )
        val pageable = PageRequest.of(page, size, validatedSort)
        val usersPage = adminService.getAllUsers(pageable, userSearchCriteria);

        logger.info { "Admin retrieved ${usersPage.numberOfElements} users on page $page out of ${usersPage.totalElements} total." }

        return ResponseEntity.ok(usersPage)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createUser(
        @Valid @RequestBody createUserRequest: UserCreateRequest,
        @RequestHeader(value = "X-Request-ID", required = false) requestId: String?
    ): ResponseEntity<UserResponse> {
        logger.info { "Admin request to create user. Email: ${createUserRequest.email}, RequestId: ${requestId ?: "N/A"}" }
        val createdUser: UserResponse = adminService.createUser(createUserRequest)
        logger.info { "Admin successfully created user. ID: ${createdUser.id}, Email: ${createdUser.email}, RequestId: ${requestId ?: "N/A"}" }
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser)
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserData(
        @PathVariable userId: Long,
        @Valid @RequestBody adminUserUpdateDTO: AdminUpdateUserRequest
    ): ResponseEntity<UserResponse> {
        logger.info { "Admin request to update user data for ID: $userId" }
        val updatedUser: UserResponse = adminService.updateUser(userId, adminUserUpdateDTO)
        logger.info { "Admin successfully updated user data for ID: $userId" }

        return ResponseEntity.ok(updatedUser)
    }

    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserAccountStatus(
        @PathVariable userId: Long,
        @Valid @RequestBody changeAccountStatusDTO: ChangeAccountStatusRequest
    ): ResponseEntity<UserResponse> {

        logger.info { "Admin request to update account status for ID: $userId to active=${changeAccountStatusDTO.isActive}" }
        val updatedUser: UserResponse = adminService.updateAccountStatus(
            userId,
            changeAccountStatusDTO
        )

        logger.info { "Admin successfully updated account status for ID: $userId" }
        return ResponseEntity.ok(updatedUser)
    }

    @PutMapping("/{userId}/authorities")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateUserAuthorities(
        @PathVariable userId: Long,
        @Valid @RequestBody adminChangeUserAuthorityRequest: AdminChangeUserAuthorityRequest
    ): ResponseEntity<UserResponse> {
        logger.info { "Admin request to update authorities for user ID: $userId" }
        val updatedUser = adminService.updateUserAuthorities(userId, adminChangeUserAuthorityRequest)
        logger.info { "Admin successfully updated authorities for user ID: $userId" }
        return ResponseEntity.ok(updatedUser)
    }

    @DeleteMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT) // Good for successful delete without a response body
    fun deleteUserAccount(@PathVariable userId: Long) {
        logger.info { "Admin request to delete user account for ID: $userId" }
        adminService.deleteUser(userId)
        logger.info { "Admin successfully deleted user account for ID: $userId" }
    }


}