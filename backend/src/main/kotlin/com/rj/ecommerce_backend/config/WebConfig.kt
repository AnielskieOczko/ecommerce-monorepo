package com.rj.ecommerce_backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.support.PathMatchingResourcePatternResolver
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    // Inject the type-safe properties object instead of using @Value
    private val storageProperties: StorageProperties
) : WebMvcConfigurer {

    /**
     * Configures resource handlers to serve static files.
     * - Serves product images from the external storage location defined in application.yml.
     * - Serves fallback/default images from the application's classpath.
     */
    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Use the injected properties object for a type-safe and clear configuration.
        val externalStoragePath = "file:${storageProperties.location}/"

        // Serve product images from the configured external storage location.
        registry.addResourceHandler("/api/v1/public/products/images/**")
            .addResourceLocations(externalStoragePath)
            .setCachePeriod(3600)

        // Also serve product images from static resources within the JAR.
        registry.addResourceHandler("/product-images/**")
            .addResourceLocations("classpath:/static/product-images/")
            .setCachePeriod(3600)
    }

    /**
     * Provides a bean needed by TestDataLoader for finding classpath resources
     * during application startup to initialize test products.
     */
    @Bean
    fun resourcePatternResolver(): PathMatchingResourcePatternResolver {
        return PathMatchingResourcePatternResolver()
    }
}