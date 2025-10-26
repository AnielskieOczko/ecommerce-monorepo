package com.rj.ecommerce_backend.notification.messaging.monitoring

import com.rj.notification_service.config.AppProperties
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

private val log = KotlinLogging.logger {}

@Service
@ConditionalOnProperty(name = ["app.monitoring.enabled"], havingValue = "true", matchIfMissing = true)
class MessageMonitoringService(
    private val rabbitAdmin: RabbitAdmin,
    private val appProperties: AppProperties
) {

    /**
     * A scheduled task that dynamically checks the status of all configured queues.
     */
    @Scheduled(cron = "\${app.monitoring.schedule}")
    fun monitorQueues() {
        log.trace { "Running scheduled queue monitoring task." }

        // Dynamically get all queue configurations from properties
        val queuesToMonitor = listOf(
            appProperties.rabbitmq.notificationRequest,
            appProperties.rabbitmq.notificationReceipt
        )

        queuesToMonitor.forEach { queueConfig ->
            // Check the main queue
            checkQueueStatus(queueConfig.queue)

            // If the queue has a DLQ configured, check it too
            queueConfig.dlq?.let {
                val dlqName = "${queueConfig.queue}.dlq"
                checkQueueStatus(dlqName, isDlq = true)
            }
        }
    }

    /**
     * Checks a single queue and logs its status.
     */
    private fun checkQueueStatus(queueName: String, isDlq: Boolean = false) {
        try {
            val queueProperties = rabbitAdmin.getQueueProperties(queueName)

            val messageCount = queueProperties[RabbitAdmin.QUEUE_MESSAGE_COUNT] as? Int ?: 0
            val consumerCount = queueProperties[RabbitAdmin.QUEUE_CONSUMER_COUNT] as? Int ?: 0

            if (isDlq) {
                log.info { "DLQ '$queueName' Status: $messageCount messages, $consumerCount consumers." }
                if (messageCount > 0) {
                    // This is a critical alert for a DLQ
                    log.error { "ALERT: Dead-Letter Queue '$queueName' has $messageCount messages requiring attention!" }
                }
            } else {
                log.info { "Queue '$queueName' Status: $messageCount messages, $consumerCount consumers." }
                if (consumerCount == 0 && messageCount > 0) {
                    // This is a warning for a regular queue
                    log.warn { "WARNING: Queue '$queueName' has $messageCount messages but no active consumers!" }
                }
            }
        } catch (e: Exception) {
            log.error(e) { "Failed to get status for queue '$queueName'." }
        }
    }
}