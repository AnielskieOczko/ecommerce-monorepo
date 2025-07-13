package com.rj.ecommerce_backend.payment


import com.rj.ecommerce.api.shared.dto.payment.request.PaymentSessionUrlRequest
import com.rj.ecommerce.api.shared.dto.payment.response.PaymentOptionDetails
import com.rj.ecommerce.api.shared.dto.payment.response.PaymentSessionResponse
import com.rj.ecommerce.api.shared.dto.payment.response.PaymentStatusResponse
import com.rj.ecommerce_backend.security.SecurityContext
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/payments")
class PaymentController(
    private val securityContext: SecurityContext,
    private val paymentFacade: PaymentFacade,
    private val paymentOptionsService: PaymentOptionsService
) {

    companion object {
        private val log = KotlinLogging.logger {}
    }


    @GetMapping("/options")
    fun getPaymentOptions(): ResponseEntity<List<PaymentOptionDetails>> {
        // This call now triggers the RabbitMQ request-reply flow.
        return ResponseEntity.ok(paymentOptionsService.getAvailablePaymentOptions())
    }

    @GetMapping("/checkout/session/{orderId}")
    fun getPaymentStatus(@PathVariable orderId: Long): ResponseEntity<PaymentStatusResponse> {
        log.info { "Fetching payment status for orderId: $orderId" }
        val paymentStatus = paymentFacade.getOrderPaymentStatus(orderId)
        return ResponseEntity.ok(paymentStatus)
    }

    @PostMapping("/checkout/session/{orderId}")
    fun createOrGetCheckoutSession(
        @PathVariable orderId: Long,
        @RequestBody request: PaymentSessionUrlRequest
    ): ResponseEntity<PaymentSessionResponse> {
        log.info { "Request to create/get checkout session for orderId: $orderId" }
        val sessionDto = paymentFacade.createCheckoutSession(
            orderId,
            request.successUrl,
            request.cancelUrl
        )
        return ResponseEntity.ok(sessionDto)
    }

}