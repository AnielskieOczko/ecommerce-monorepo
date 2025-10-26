package com.rj.ecommerce_backend.security.config

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "jwt")
@EnableScheduling
class JwtConfig {

    @field:NotBlank(message = "JWT secret cannot be blank.")
    lateinit var secret: String

    @field:Min(value = 300000, message = "JWT expiration (expirationMs) must be at least 5 minutes (300000 ms).")
    val expirationMs: Int = 0

    @field:Valid
    var cleanup: Cleanup = Cleanup()


    @Validated
    data class Cleanup(
        @field:NotBlank(message = "Cleanup cron expression cannot be blank.")

        var cron: String = "0 0 * * * *", // Default: every hour at minute 0, second 0

        @field:Min(value = 1, message = "Cleanup batch size must be at least 1.")
        var batchSize: Int = 1000 // Default
    )
}

