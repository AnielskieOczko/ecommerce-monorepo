package com.rj.payment_service.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

private val logger = KotlinLogging.logger { WebhookDispatcher::class }

@Service
class WebhookDispatcher(strategies: List<PaymentProviderStrategy>) {
    private val strategyMap = strategies.associateBy { it.getProviderIdentifier() }

    fun dispatch(provider: String, payload: String, signature: String?) {
        val strategy = strategyMap[provider.uppercase()]
            ?: run {
                logger.warn { "Received webhook for unknown provider: $provider" }
                return
            }
        strategy.handleWebhook(payload, signature)
    }
}