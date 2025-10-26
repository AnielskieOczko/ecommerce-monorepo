package com.rj.notification_service.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.convertValue
import com.rj.ecommerce.api.shared.enums.NotificationTemplate
import com.rj.notification_service.exception.TemplateProcessingException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.spring6.SpringTemplateEngine

private val log = KotlinLogging.logger {}

@Service
class TemplateService(
    private val templateEngine: SpringTemplateEngine,
    private val objectMapper: ObjectMapper // Use a standard Jackson mapper
) {

    /**
     * Renders an HTML string from a given template and payload.
     *
     * @param template The NotificationTemplate enum constant, which provides the template file name.
     * @param payload The data payload (e.g., OrderPayload, WelcomePayload) containing the context for the template.
     * @return A String containing the rendered HTML.
     * @throws TemplateProcessingException if the template rendering fails.
     */
    fun renderHtml(template: NotificationTemplate, payload: Any): String {
        log.debug { "Rendering template '${template.templateId}' for payload of type ${payload::class.simpleName}" }
        try {
            // 1. Convert the payload DTO into a Map that Thymeleaf can use.
            // This is a clean way to make all the payload's properties available in the template.
            val contextMap: Map<String, Any> = objectMapper.convertValue(payload)

            // 2. Create the Thymeleaf context and populate it with the map.
            val context = Context()
            context.setVariables(contextMap)

            // 3. Process the template and return the HTML string.
            // The templateId from our enum maps directly to the file name (e.g., "order-confirmation.html").
            return templateEngine.process(template.templateId, context)
        } catch (e: Exception) {
            log.error(e) { "Failed to render template '${template.templateId}'" }
            // Wrap the original exception to provide more context.
            throw TemplateProcessingException("Error rendering template: ${template.templateId}", e)
        }
    }
}