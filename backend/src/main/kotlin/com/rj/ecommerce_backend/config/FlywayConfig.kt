package com.rj.ecommerce_backend.config

import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig {

    /**
     * Customizes Flyway's configuration.
     * This setup ensures that Flyway will create a baseline (a starting point in its schema_version table)
     * if it runs against a database that already has tables but no schema_version table yet.
     */
    @Bean
    fun flywayConfigurationCustomizer(): FlywayConfigurationCustomizer {
        return FlywayConfigurationCustomizer { configuration ->
            configuration
                .baselineOnMigrate(true)
                .baselineDescription("Initial Baseline")
                .baselineVersion("0")
            // The defaultSchema is now controlled by application.yml (spring.flyway.default-schema)
        }
    }
}