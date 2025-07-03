// File: email-service/src/main/kotlin/com/rj/ecommerce_email_service/messaging/admin/QueueAdminController.kt
package com.rj.notification_service.messaging.admin

import com.rj.notification_service.config.AppProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("/admin/queues")
class QueueAdminController(
    private val rabbitAdmin: RabbitAdmin,
    private val appProperties: AppProperties
) {

    @DeleteMapping("/purge/{queueName}")
    fun purgeQueue(@PathVariable queueName: String): ResponseEntity<String> {
        log.warn { "Received admin request to purge queue: '$queueName'" }

        // Use the type-safe properties to find the actual queue name.
        val queueToPurge = when (queueName.lowercase()) {
            "email" -> appProperties.rabbitmq.notificationRequest.queue
            "dlq" -> "${appProperties.rabbitmq.notificationRequest.queue}.dlq"
            "receipt" -> appProperties.rabbitmq.notificationReceipt.queue
            else -> {
                log.error { "Attempted to purge an unknown queue alias: '$queueName'" }
                return ResponseEntity.badRequest().body("Unknown queue alias: '$queueName'. Known aliases: email, dlq, receipt.")
            }
        }

        val messageCount = rabbitAdmin.purgeQueue(queueToPurge, false)
        val responseMessage = "Queue '$queueToPurge' has been purged. $messageCount messages were deleted."
        log.info { responseMessage }

        return ResponseEntity.ok(responseMessage)
    }

    @DeleteMapping("/purge-all")
    fun purgeAllQueues(): ResponseEntity<String> {
        log.warn { "Received admin request to purge ALL known queues." }

        val emailQueue = appProperties.rabbitmq.notificationRequest.queue
        val dlq = "$emailQueue.dlq"
        val receiptQueue = appProperties.rabbitmq.notificationReceipt.queue

        rabbitAdmin.purgeQueue(emailQueue)
        rabbitAdmin.purgeQueue(dlq)
        rabbitAdmin.purgeQueue(receiptQueue)

        val responseMessage = "All known queues ($emailQueue, $dlq, $receiptQueue) have been purged."
        log.info { responseMessage }

        return ResponseEntity.ok(responseMessage)
    }
}