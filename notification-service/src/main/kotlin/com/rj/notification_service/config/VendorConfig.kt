package com.rj.notification_service.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VendorConfig(private val props: AppProperties) {

    @Bean
    fun smtpConfig(): AppProperties.SmtpConfig? {
        return props.notification.vendors.smtp
    }

    // It's good practice to add the others now too
    @Bean
    fun sendGridConfig(): AppProperties.SendGridConfig? {
        return props.notification.vendors.sendgrid
    }

    @Bean
    fun twilioConfig(): AppProperties.TwilioConfig? {
        return props.notification.vendors.twilio
    }

}