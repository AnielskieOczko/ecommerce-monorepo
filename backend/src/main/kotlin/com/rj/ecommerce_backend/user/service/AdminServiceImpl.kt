package com.rj.ecommerce_backend.user.service

import com.rj.ecommerce.api.shared.core.Address
import com.rj.ecommerce.api.shared.core.Email
import com.rj.ecommerce.api.shared.core.Password
import com.rj.ecommerce.api.shared.core.ZipCode
import com.rj.ecommerce.api.shared.dto.user.request.ChangeAccountStatusRequest
import com.rj.ecommerce.api.shared.dto.user.request.AdminChangeUserAuthorityRequest
import com.rj.ecommerce.api.shared.dto.user.request.AdminUpdateUserRequest
import com.rj.ecommerce.api.shared.dto.user.request.UserCreateRequest
import com.rj.ecommerce.api.shared.dto.user.response.UserResponse
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.user.domain.Authority
import com.rj.ecommerce_backend.user.domain.User
import com.rj.ecommerce_backend.user.exception.AuthorityNotFoundException
import com.rj.ecommerce_backend.user.exception.EmailAlreadyExistsException
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import com.rj.ecommerce_backend.user.mapper.UserMapper
import com.rj.ecommerce_backend.user.repository.AuthorityRepository
import com.rj.ecommerce_backend.user.repository.UserRepository
import com.rj.ecommerce_backend.user.search.UserSearchCriteria
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.collections.emptySet

@Service
@Transactional
class AdminServiceImpl(
    private val userRepository: UserRepository,
    private val authorityRepository: AuthorityRepository,
    private val passwordEncoder: PasswordEncoder,
    private val userMapper: UserMapper,
    private val securityContext: SecurityContext
) : AdminService {

    companion object {
        private const val USER_NOT_FOUND_MSG_PREFIX = "User not found for id: "
        private const val AUTH_NOT_FOUND_MSG = "One or more specified authorities not found."
        private val logger = KotlinLogging.logger { }
    }

    @Transactional(readOnly = true)
    override fun getAllUsers(
        pageable: Pageable,
        criteria: UserSearchCriteria
    ): Page<UserResponse> {
        logger.info { "Admin retrieving users with search criteria: $criteria" }

        securityContext.ensureAdmin()

        val spec: Specification<User> = criteria.toSpecification()
        val usersPage: Page<User> = userRepository.findAll(spec, pageable)

        return usersPage.map { user -> userMapper.toUserResponse(user) }

    }

    @Transactional(readOnly = true)
    override fun getUserById(userId: Long): UserResponse {
        logger.info { "Admin retrieving user with ID: $userId" }
        securityContext.ensureAdmin()

        return userMapper.toUserResponse(findUserByIdOrThrow(userId))

    }

    @Transactional
    override fun createUser(request: UserCreateRequest): UserResponse {
        logger.info { "Processing new request for user creation: $request" }

        val newEmailVO = Email(request.email) // Throws if format is invalid
        if (userRepository.existsByEmail(newEmailVO)) {
            throw EmailAlreadyExistsException("Email '${request.email}' is already in use.")
        }

        val user = User(
            firstName = request.userDetails.firstName,
            lastName = request.userDetails.lastName,
            email = newEmailVO,
            password = Password(
                passwordEncoder.encode(request.password)
            ),
            address = Address(
                street = request.userDetails.address?.street,
                city = request.userDetails.address?.city,
                zipCode = ZipCode(request.userDetails.address?.zipCode?.value)
            ),
            phoneNumber = request.userDetails.phoneNumber,
            dateOfBirth = request.userDetails.dateOfBirth,
            isActive = true, //by default new user is active
            authorities = mutableSetOf()
        )

        updateUserAuthoritiesInternal(user, request.authorities)

        val newUser = userRepository.save(user)
        logger.info { "New user with ID: ${newUser.id}  created." }

        return userMapper.toUserResponse(newUser)

    }

    override fun updateUser(
        userId: Long,
        request: AdminUpdateUserRequest
    ): UserResponse {
        logger.info { "Admin updating user with ID: $userId" }
        securityContext.ensureAdmin()

        val user: User = findUserByIdOrThrow(userId)

        request.email?.let { newEmailString ->
            if (newEmailString != user.email.value) {
                val newEmailVO = Email(newEmailString) // Validates format
                if (userRepository.existsByEmailAndIdNot(newEmailVO, userId)) {
                    throw EmailAlreadyExistsException("Email '${newEmailString}' is already in use by another account.")
                }
                user.email = newEmailVO // Update if all checks pass
            }
        }

        // this does not touch authorities
        userMapper.updateUserFromAdminRequest(user, request)

        updateUserAuthoritiesInternal(user, request.authorities)

        val savedUser = userRepository.save(user)
        logger.info { "Admin updated user with ID: ${savedUser.id}." }
        return userMapper.toUserResponse(savedUser)


    }

    override fun deleteUser(userId: Long) {
        logger.info { "Admin attempts to delete user with ID $userId." }
        securityContext.ensureAdmin()

        val user: User = findUserByIdOrThrow(userId)

        // Consider if associated refresh tokens should be deleted here.
        // refreshTokenRepository.deleteByUserId(user.id!!) // If User.id is non-null by this point

        userRepository.delete(user)
        logger.info { "Admin deleted user with ID: ${user.id}." }
    }

    @Transactional(readOnly = true)
    override fun getUserEntityForValidation(userId: Long): User? {
        logger.info { "Admin retrieves user entity with ID: $userId for validation." }
        securityContext.ensureAdmin()
        return userRepository.findById(userId).orElse(null)
    }

    override fun updateAccountStatus(
        userId: Long,
        request: ChangeAccountStatusRequest
    ): UserResponse {
        logger.info { "Admin attempts to update account status for user ID: $userId to active=${request.isActive}" }
        securityContext.ensureAdmin()

        val user: User = findUserByIdOrThrow(userId)

        val oldStatus: Boolean = user.isActive

        user.isActive = request.isActive
        val savedUser = userRepository.save(user)

        logger.info { "Admin updated account's status for user ID: $user.id from $oldStatus to ${user.isActive}" }

        return userMapper.toUserResponse(savedUser)

    }

    override fun updateUserAuthorities(
        userId: Long,
        request: AdminChangeUserAuthorityRequest
    ): UserResponse {
        logger.info { "Admin explicitly updating authorities for user ID: $userId" }
        securityContext.ensureAdmin()

        val user: User = findUserByIdOrThrow(userId)

        // update authorities
        updateUserAuthoritiesInternal(user, request.authorities)

        val savedUser = userRepository.save(user)
        logger.info { "Admin explicitly updated authorities for user ID: ${user.id}." }
        return userMapper.toUserResponse(savedUser)
    }

    override fun enableUsers(userIds: List<Long>): Int {
        logger.info { "Admin request to enable users: $userIds" }
        securityContext.ensureAdmin()
        val users = userRepository.findAllById(userIds)

        if (users.size != userIds.size) {
            logger.warn { "Some user IDs provided for enabling were not found." }
            // Potentially throw or just proceed with found users
        }

        users.forEach { user -> user.isActive = true }

        userRepository.saveAll(users)
        logger.info { "Enabled ${users.size} users." }
        return users.size
    }

    override fun disableUsers(userIds: List<Long>): Int {
        logger.info { "Admin request to disable users: $userIds" }
        securityContext.ensureAdmin()
        val users = userRepository.findAllById(userIds)

        if (users.size != userIds.size) {
            logger.warn { "Some user IDs provided for disabling were not found." }
            // Potentially throw or just proceed with found users
        }

        users.forEach { user -> user.isActive = false }

        userRepository.saveAll(users)
        logger.info { "Disabled ${users.size} users." }
        return users.size
    }

    override fun deleteUsers(userIds: List<Long>): Int {
        logger.info { "Admin request to delete users: $userIds" }
        securityContext.ensureAdmin()
        val users = userRepository.findAllById(userIds) // Fetch to ensure they exist and for potential pre-delete logic
        if (users.isNotEmpty()) {

            // TODO: Consider deleting associated data like refresh tokens in bulk or per user
            // TODO: users.forEach { user -> refreshTokenRepository.deleteByUserId(user.id!!) }
            userRepository.deleteAll(users) // Or userRepository.deleteAllById(userIds)
            logger.info { "Deleted ${users.size} users." }
        } else {
            logger.info { "No users found for the provided IDs to delete." }
        }
        return users.size
    }

    /**
     * Private helper to find a user by ID or throw UserNotFoundException.
     * Also logs a warning if not found, including the current authenticated user for context.
     */
    private fun findUserByIdOrThrow(userId: Long): User {
        return userRepository.findById(userId)
            .orElseThrow {
                // It's good to know WHO tried to access a non-existent user after passing initial security checks
                val currentAuthUser = try {
                    securityContext.getCurrentUser().id
                } catch (_: Exception) {
                    "UNKNOWN_OR_ANONYMOUS"
                }
                logger.warn { "$USER_NOT_FOUND_MSG_PREFIX$userId (operation initiated by authenticated user: $currentAuthUser)" }
                UserNotFoundException("$USER_NOT_FOUND_MSG_PREFIX$userId")
            }
    }


    /**
     * Updates the authorities for a given user based on a set of role names.
     * - If newAuthorityNames is null, the user's authorities are not changed.
     * - If newAuthorityNames is empty, all existing authorities are cleared from the user.
     * - If newAuthorityNames contains names, it fetches the corresponding Authority entities
     *   and replaces the user's current authorities with these new ones.
     *
     * @throws AuthorityNotFoundException if any of the provided authority names are not found.
     */
    private fun updateUserAuthoritiesInternal(user: User, newAuthorityNames: Set<String>?) {
        if (newAuthorityNames == null) {
            logger.debug { "No authority changes requested for user ID: ${user.id}. Current authorities preserved." }
            return
        }

        val newAuthorities: Set<Authority> = if (newAuthorityNames.isNotEmpty()) {
            val fetched = authorityRepository.findByNameIn(newAuthorityNames)
            if (fetched.size != newAuthorityNames.size) {
                val requestedNames = newAuthorityNames
                val foundNames = fetched.map { it.name }.toSet()
                val missingNames = requestedNames - foundNames
                logger.warn { "Could not find all specified authorities for user ID ${user.id}. Requested: $requestedNames, Found: $foundNames, Missing: $missingNames" }
                throw AuthorityNotFoundException("$AUTH_NOT_FOUND_MSG Missing: $missingNames")
            }
            fetched
        } else {
            // Admin provided an empty set, meaning clear all authorities.
            logger.debug { "Clearing all authorities for user ID: ${user.id} as requested." }
            emptySet()
        }

        val currentUserAuthorityNames = user.authorities.map { it.name }.toSet()

        if (currentUserAuthorityNames == newAuthorityNames) {
            val oldAuthoritiesDisplay = user.authorities.map { it.name }.toSet() // For logging

            val authoritiesToRemove = user.authorities.filterNot { newAuthorities.contains(it) }.toSet()
            val authoritiesToAdd = newAuthorities.filterNot { user.authorities.contains(it) }.toSet()

            authoritiesToRemove.forEach { authority -> user.removeAuthority(authority) }
            authoritiesToAdd.forEach { authority -> user.addAuthority(authority) }

            logger.info {
                "Authorities updated for user ID: ${user.id}. Old: $oldAuthoritiesDisplay, New: ${
                    newAuthorities.map { it.name }.toSet()
                }"
            }
        } else {
            logger.debug {
                "No effective change in authorities for user ID: ${user.id}. Authorities remain: ${
                    user.authorities.map { it.name }.toSet()
                }"
            }
        }


    }
}