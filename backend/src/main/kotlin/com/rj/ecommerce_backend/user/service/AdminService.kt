package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.dto.user.AccountStatusRequestDTO
import com.rj.ecommerce.api.shared.dto.user.AdminChangeUserAuthorityRequest
import com.rj.ecommerce.api.shared.dto.user.AdminUpdateUserRequest
import com.rj.ecommerce.api.shared.dto.user.UserCreateRequestDTO
import com.rj.ecommerce.api.shared.dto.user.UserInfoDTO
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.search.UserSearchCriteria
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface AdminService {
    fun getAllUsers(pageable: Pageable, criteria: UserSearchCriteria): Page<UserInfoDTO>
    fun getUserById(userId: Long): UserInfoDTO
    fun createUser(request: UserCreateRequestDTO): UserInfoDTO
    fun updateUser(userId: Long, request: AdminUpdateUserRequest): UserInfoDTO
    fun deleteUser(userId: Long)


    /**
     * Internal helper to fetch a User entity, primarily for validation or internal operations.
     * Returns nullable User as it might not be found, and the caller should handle that.
     * Consider if this should throw UserNotFoundException if not found, depending on usage.
     * If always expected to be found by callers, then User (non-nullable) return with internal throw.
     */
    fun getUserEntityForValidation(userId: Long): User?

    fun updateAccountStatus(userId: Long, request: AccountStatusRequestDTO): UserInfoDTO
    fun updateUserAuthorities(userId: Long, request: AdminChangeUserAuthorityRequest): UserInfoDTO

    // bulk operations
    fun enableUsers(userIds: List<Long>): Int // Consider returning number of users affected or List<UserInfoDTO>

    fun disableUsers(userIds: List<Long>): Int // Consider returning number of users affected or List<UserInfoDTO>

    fun deleteUsers(userIds: List<Long>): Int // Consider returning number of users deleted


}