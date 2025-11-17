package com.rj.ecommerce_backend.payment.controller

import com.rj.ecommerce_backend.payment.provider.WebhookProviderDispatcher
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks")
class WebhookController(private val dispatcher: WebhookProviderDispatcher) {
    @PostMapping("/{provider}")
    fun handleWebhook(
        @PathVariable provider: String,
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String?
    ) {
        dispatcher.dispatch(provider, payload, signature)
    }
}