package com.rj.ecommerce_backend.payment.provider

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger {}

@Service
class WebhookProviderDispatcher(providers: List<PaymentProvider>) {
    private val providerMap = providers.associateBy { it.getProviderIdentifier() }

    fun dispatch(providerIdentifier: String, payload: String, signature: String?) {
        val provider = providerMap[providerIdentifier.uppercase()]
            ?: run {
                logger.warn { "Received webhook for unknown provider: $providerIdentifier" }
                return
            }

        logger.info { "Dispatching webhook for provider: $providerIdentifier" }
        provider.handleWebhook(payload, signature)
    }
}