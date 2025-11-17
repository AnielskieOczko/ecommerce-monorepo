package com.rj.ecommerce_backend.notification.provider

import com.rj.ecommerce_backend.api.shared.enums.NotificationChannel
import com.rj.ecommerce_backend.notification.command.CreateNotificationCommand
import com.rj.ecommerce_backend.notification.config.AppProperties
import com.rj.ecommerce_backend.notification.model.EmailModel
import com.rj.ecommerce_backend.notification.provider.vendor.email.EmailVendorProvider
import com.rj.ecommerce_backend.notification.provider.vendor.email.EmailVendorProviderFactory
import com.rj.ecommerce_backend.notification.service.TemplateService
import org.springframework.stereotype.Component

@Component
class EmailChannelProvider(
    private val templateService: TemplateService,
    private val appProperties: AppProperties,
    // Spring injects all email-specific vendor beans
    emailVendorProviders: List<EmailVendorProvider>
) : ChannelProvider {

    // This channel provider has its own internal factory to manage email vendors (SMTP, SendGrid, etc.)
    private val vendorFactory = EmailVendorProviderFactory(
        emailVendorProviders,
        appProperties
    )

    override fun getChannelType(): NotificationChannel = NotificationChannel.EMAIL

    override fun process(command: CreateNotificationCommand) {
        // 1. Render the HTML body using the generic payload.
        val htmlBody = templateService.renderHtml(command.template, command.context)

        val emailChannelConfig = appProperties.notification.channels["email"]
            ?: throw IllegalStateException("Configuration for the 'email' channel is missing in application.yml.")

        // 2. Create the specific model required for this channel.
        val emailModel = EmailModel(
            to = command.recipient,
            from = emailChannelConfig.defaultFrom,
            subject = command.subject,
            htmlBody = htmlBody
        )

        // 3. Get the active *vendor* provider (e.g., SmtpMailVendor) and send.
        val vendorProvider = vendorFactory.getActiveVendor()
        vendorProvider.send(emailModel)
    }
}