package com.rj.notification_service.provider

import com.rj.ecommerce.api.shared.enums.NotificationChannel
import com.rj.ecommerce.api.shared.messaging.notification.common.NotificationRequest
import com.rj.notification_service.config.AppProperties
import com.rj.notification_service.model.EmailModel
import com.rj.notification_service.provider.vendor.email.EmailVendorProvider
import com.rj.notification_service.provider.vendor.email.EmailVendorProviderFactory
import com.rj.notification_service.service.TemplateService
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
        appProperties)

    override fun getChannelType(): NotificationChannel = NotificationChannel.EMAIL

    override fun process(request: NotificationRequest<Any>) {
        // 1. Render the HTML body using the generic payload.
        val htmlBody = templateService.renderHtml(request.envelope.template, request.payload)

        val emailChannelConfig = appProperties.notification.channels["email"]
            ?: throw IllegalStateException("Configuration for the 'email' channel is missing in application.yml.")

        // 2. Create the specific model required for this channel.
        val emailModel = EmailModel(
            to = request.envelope.to,
            from = emailChannelConfig.defaultFrom,
            subject = request.envelope.subject,
            htmlBody = htmlBody
        )

        // 3. Get the active *vendor* provider (e.g., SmtpMailVendor) and send.
        val vendorProvider = vendorFactory.getActiveVendor()
        vendorProvider.send(emailModel)
    }
}