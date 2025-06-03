package com.rj.ecommerce_backend.securityconfig.config

import com.rj.ecommerce_backend.securityconfig.services.LogoutService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder

private val logger = KotlinLogging.logger { }

@Configuration
@EnableWebSecurity
class AuthTokenFilter(
    private val userDetailsService: UserDetailsService,
    private val authTokenFilter: AuthTokenFilter,
    private val logoutService: LogoutService

) {
    @Bean
    fun authenticationManager(
        authenticationConfiguration: AuthenticationConfiguration
    ): AuthenticationManager {
        return authenticationConfiguration.authenticationManager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    

}