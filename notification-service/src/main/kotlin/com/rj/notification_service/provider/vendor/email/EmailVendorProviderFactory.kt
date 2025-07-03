package com.rj.notification_service.provider.vendor.email

import com.rj.notification_service.config.AppProperties

class EmailVendorProviderFactory(
    providerBeans: List<EmailVendorProvider>,
    private val mailProperties: AppProperties.NotificationConfig
) {
    private val providers = providerBeans.associateBy { it.getProviderType() }

    fun getActiveVendor(): EmailVendorProvider {
        val providerType = mailProperties.activeProvider
        return providers[providerType]
            ?: throw IllegalStateException("No email vendor provider configured for type: $providerType")
    }
}