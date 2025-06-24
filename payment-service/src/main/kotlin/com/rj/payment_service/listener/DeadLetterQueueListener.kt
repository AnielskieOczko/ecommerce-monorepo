package com.rj.payment_service.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component

@Component
class DeadLetterQueueListener {

    companion object {
        private val logger = KotlinLogging.logger { }

    }

    @RabbitListener(queues = ["\${app.rabbitmq.dlq-queue}"])
    fun handleDeadLetterQueue(message: Message) {
        val originalQueue = message
            .messageProperties
            .headers["x-original-queue"].toString()

        val errorMessage = message
            .messageProperties
            .headers["x-original-error-message"].toString()
        // TODO:
        // Here you could:
        // 1. Send notifications to admin
        // 2. Store failed messages in database
        // 3. Implement custom recovery logic
        // 4. Forward to another service
    }
}

