package com.rj.ecommerce_backend

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@SpringBootApplication
@EnableRetry
@ConfigurationPropertiesScan
class EcommerceBackendApplication

/**
 * The main entry point for the Spring Boot application.
 * The `runApplication` function is a helper from `spring-boot-runapplication`
 * that provides a concise way to start the app.
 */
fun main(args: Array<String>) {
    runApplication<EcommerceBackendApplication>(*args)
}