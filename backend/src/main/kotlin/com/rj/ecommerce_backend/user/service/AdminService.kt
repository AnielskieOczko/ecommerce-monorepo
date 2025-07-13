package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.user.request.ChangeAccountStatusRequest
import com.rj.ecommerce.api.shared.dto.user.request.AdminChangeUserAuthorityRequest
import com.rj.ecommerce.api.shared.dto.user.request.AdminUpdateUserRequest
import com.rj.ecommerce.api.shared.dto.user.request.UserCreateRequest
import com.rj.ecommerce.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.search.UserSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AdminService {
    fun getAllUsers(pageable: Pageable, criteria: UserSearchCriteria): Page<UserResponse>
    fun getUserById(userId: Long): UserResponse
    fun createUser(request: UserCreateRequest): UserResponse
    fun updateUser(userId: Long, request: AdminUpdateUserRequest): UserResponse
    fun deleteUser(userId: Long)


    /**
     * Internal helper to fetch a User entity, primarily for validation or internal operations.
     * Returns nullable User as it might not be found, and the caller should handle that.
     * Consider if this should throw UserNotFoundException if not found, depending on usage.
     * If always expected to be found by callers, then User (non-nullable) return with internal throw.
     */
    fun getUserEntityForValidation(userId: Long): User?

    fun updateAccountStatus(userId: Long, request: ChangeAccountStatusRequest): UserResponse
    fun updateUserAuthorities(userId: Long, request: AdminChangeUserAuthorityRequest): UserResponse

    // bulk operations
    fun enableUsers(userIds: List<Long>): Int // Consider returning number of users affected or List<UserResponse>

    fun disableUsers(userIds: List<Long>): Int // Consider returning number of users affected or List<UserResponse>

    fun deleteUsers(userIds: List<Long>): Int // Consider returning number of users deleted


}