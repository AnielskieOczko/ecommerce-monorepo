package com.rj.payment_service.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

/**
 * Centralized configuration for Jackson ObjectMapper instances.
 * This class defines separate mappers for general application use (e.g., REST APIs)
 * and for RabbitMQ messaging to ensure security and proper serialization behavior.
 */
@Configuration
class JacksonConfig {

    /**
     * Defines the primary ObjectMapper for general application use, such as Spring MVC's
     * request/response body serialization. This is the default bean injected by Spring.
     *
     * It is configured for resilience and security:
     * - Disables `FAIL_ON_UNKNOWN_PROPERTIES` to prevent errors if clients send extra data.
     * - **Crucially, it does NOT enable default typing**, avoiding security vulnerabilities.
     */
    @Bean
    @Primary
    fun primaryObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
    }

    /**
     * Defines a specialized ObjectMapper for RabbitMQ serialization.
     * This mapper is intended to be injected specifically where needed for messaging.
     *
     * It is configured for polymorphic deserialization:
     * - Enables `activateDefaultTyping` to include class information (@class property) in the
     *   JSON payload. This is essential for correctly deserializing interfaces (like
     *   `EcommerceEmailRequest`) into their concrete implementations on the consumer side.
     */
    @Bean
    @Qualifier("rabbitObjectMapper")
    fun rabbitObjectMapper(): ObjectMapper {
        return ObjectMapper().apply {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

            activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.PROPERTY
            )
        }
    }
}