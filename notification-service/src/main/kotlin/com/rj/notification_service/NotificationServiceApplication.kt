package com.rj.notification_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication


@SpringBootApplication
@ConfigurationPropertiesScan
class NotificationServiceApplication

fun main(args: Array<String>) {
    // This starts the Spring Boot application.
    runApplication<NotificationServiceApplication>(*args)
}