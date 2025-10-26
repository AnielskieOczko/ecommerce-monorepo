package com.rj.ecommerce_backend.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {

    /**
     * Configures the global OpenAPI metadata for the Swagger UI documentation.
     * This includes title, version, and contact information.
     */
    @Bean
    fun ecommerceOpenApi(): OpenAPI {
        return OpenAPI().info(
            Info().apply {
                title = "E-commerce API Documentation"
                description = "REST API documentation for the E-commerce application"
                version = "v1.0.0"
                contact = Contact().apply {
                    name = "Rafał Jankowski"
                    email = "rafaljankowski7@gmail.com"
                }
            }
        )
    }
}