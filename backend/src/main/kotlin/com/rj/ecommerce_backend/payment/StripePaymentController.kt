package com.rj.ecommerce_backend.payment

import com.rj.ecommerce.api.shared.messaging.payment.CheckoutSessionDTO
import com.rj.ecommerce.api.shared.messaging.payment.CheckoutUrlsRequestDTO
import com.rj.ecommerce.api.shared.messaging.payment.PaymentStatusDTO
import com.rj.ecommerce_backend.security.SecurityContext
import com.rj.ecommerce_backend.security.exception.UserAuthenticationException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/stripe")
class StripePaymentController(
    private val securityContext: SecurityContext,
    private val stripePaymentService: StripePaymentService
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }

    @GetMapping("/checkout/session/{orderId}")
    fun getPaymentStatus(@PathVariable orderId: Long): ResponseEntity<PaymentStatusDTO> {
        log.info { "Fetching payment status for orderId: $orderId" }
        val paymentStatus = stripePaymentService.getOrderPaymentStatus(orderId)
        return ResponseEntity.ok(paymentStatus)
    }

    @PostMapping("/checkout/session/{orderId}")
    fun createOrGetCheckoutSession(
        @PathVariable orderId: Long,
        @RequestBody request: CheckoutUrlsRequestDTO
    ): ResponseEntity<CheckoutSessionDTO> {
        log.info { "Request to create/get checkout session for orderId: $orderId" }
        // 2. Logic is clean and focuses on the "happy path".
        val userId = getCurrentUserId()
        val sessionDto = stripePaymentService.createOrGetCheckoutSession(
            userId,
            orderId,
            request.successUrl,
            request.cancelUrl
        )
        return ResponseEntity.ok(sessionDto)
    }

    /**
     * Private helper to encapsulate user retrieval and make the controller method cleaner.
     */
    private fun getCurrentUserId(): Long {
        return securityContext.getCurrentUser().id
            ?: throw UserAuthenticationException("User is not authenticated or does not have an ID.")
    }
}