package com.rj.ecommerce_backend.notification.service

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.rj.ecommerce_backend.api.shared.enums.NotificationTemplate
import com.rj.ecommerce_backend.notification.context.NotificationContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import org.thymeleaf.context.Context
import org.thymeleaf.exceptions.TemplateProcessingException
import org.thymeleaf.spring6.SpringTemplateEngine

private val log = KotlinLogging.logger {}

@Service
class TemplateService(
    private val templateEngine: SpringTemplateEngine,
    private val objectMapper: ObjectMapper
) {

    /**
     * Renders an HTML string from a given template and payload.
     *
     * @param template The NotificationTemplate enum constant, which provides the template file name.
     * @param context The notificationContext representing possible data contexts
     * @return A String containing the rendered HTML.
     * @throws TemplateProcessingException if the template rendering fails.
     */
    fun renderHtml(template: NotificationTemplate, context: NotificationContext): String {
        log.debug { "Rendering template '${template.templateId}' for context ${context::class.simpleName}" }
        try {
            val contextMap: Map<String, Any> = when(context) {
                is NotificationContext.OrderContext -> objectMapper.convertValue(context.order, object : TypeReference<Map<String, Any>>() {})
                is NotificationContext.OrderStatusUpdateContext -> objectMapper.convertValue(mapOf("order" to context.order, "previousStatus" to context.previousStatus), object : TypeReference<Map<String, Any>>() {})
                is NotificationContext.EmptyContext -> emptyMap()
            }

            // 2. Create the Thymeleaf context and populate it with the map.
            val thymeleafContext = Context()
            thymeleafContext.setVariables(contextMap)

            // 3. Process the template and return the HTML string.
            // The templateId from our enum maps directly to the file name (e.g., "order-confirmation.html").
            return templateEngine.process(template.templateId, thymeleafContext)
        } catch (e: Exception) {
            log.error(e) { "Failed to render template '${template.templateId}'" }
            // Wrap the original exception to provide more context.
            throw TemplateProcessingException("Error rendering template: ${template.templateId}", e)
        }
    }
}