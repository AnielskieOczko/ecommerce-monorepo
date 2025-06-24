package com.rj.payment_service.controller

import com.rj.payment_service.service.WebhookDispatcher
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/webhooks")
class WebhookController(private val dispatcher: WebhookDispatcher) {
    @PostMapping("/{provider}")
    fun handleWebhook(
        @PathVariable provider: String,
        @RequestBody payload: String,
        @RequestHeader("Stripe-Signature") signature: String? // Or a generic signature header
    ) {
        dispatcher.dispatch(provider, payload, signature)
    }
}