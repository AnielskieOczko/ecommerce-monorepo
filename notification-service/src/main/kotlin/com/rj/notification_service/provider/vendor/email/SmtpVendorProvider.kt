package com.rj.notification_service.provider.vendor.email

import com.rj.notification_service.config.AppProperties
import com.rj.notification_service.model.EmailModel
import jakarta.annotation.PostConstruct
import jakarta.mail.internet.MimeMessage
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.JavaMailSenderImpl
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Component
import java.util.*

@Component
// This bean will only be created if a configuration for it exists.
@ConditionalOnProperty(name = ["app.notification.vendors.smtp.host"])
class SmtpVendorProvider(
    private val smtpConfig: AppProperties.SmtpConfig
) : EmailVendorProvider {

    // The mail sender is now a private property of this class, not a shared bean.
    private lateinit var mailSender: JavaMailSender

    @PostConstruct
    fun init() {
        // Create and configure the mail sender instance when this bean is created.
        val senderImpl = JavaMailSenderImpl()
        senderImpl.host = smtpConfig.host
        senderImpl.port = smtpConfig.port
        senderImpl.protocol = smtpConfig.protocol

        if (!smtpConfig.username.isNullOrBlank()) {
            senderImpl.username = smtpConfig.username
            senderImpl.password = smtpConfig.password
        }

        val props = Properties()
        props.putAll(smtpConfig.properties)
        senderImpl.javaMailProperties = props

        this.mailSender = senderImpl
    }

    override fun getProviderType(): String = "smtp"

    override fun send(email: EmailModel) {
        try {
            val message: MimeMessage = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(email.from)
            helper.setTo(email.to)
            helper.setSubject(email.subject)
            helper.setText(email.htmlBody, true)
            mailSender.send(message)
        } catch (e: Exception) {
            throw RuntimeException("Failed to send email via SMTP for recipient ${email.to}", e)
        }
    }
}