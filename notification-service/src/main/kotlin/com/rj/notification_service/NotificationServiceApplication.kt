package com.rj.notification_service

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// This class is a marker for Spring's configuration and component scanning.
// It can be empty.
@SpringBootApplication
class NotificationServiceApplication

// The main function must be a TOP-LEVEL function, outside the class.
fun main(args: Array<String>) {
    // This starts the Spring Boot application.
    runApplication<NotificationServiceApplication>(*args)
}