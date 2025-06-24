package com.rj.payment_service.exception

import com.rj.ecommerce.api.shared.core.ErrorDTO // Use the shared DTO
import com.stripe.exception.StripeException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.LocalDateTime

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(PaymentProcessingException::class)
    fun handlePaymentProcessingException(e: PaymentProcessingException): ResponseEntity<ErrorDTO> {
        val errorDto = ErrorDTO(
            status = HttpStatus.BAD_REQUEST.value(),
            message = "PAYMENT_PROCESSING_ERROR: ${e.message}",
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity.badRequest().body(errorDto)
    }

    @ExceptionHandler(StripeException::class)
    fun handleStripeException(e: StripeException): ResponseEntity<ErrorDTO> {
        val httpStatus = HttpStatus.resolve(e.statusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR

        val errorCode = when (e.code) {
            "card_declined" -> "CARD_ERROR"
            "invalid_request_error" -> "INVALID_REQUEST_ERROR"
            "authentication_error" -> "AUTHENTICATION_ERROR"
            // Add more specific Stripe error codes as needed
            else -> "STRIPE_API_ERROR"
        }

        val errorDto = ErrorDTO(
            status = httpStatus.value(),
            message = "$errorCode: ${e.message}",
            timestamp = LocalDateTime.now()
        )

        return ResponseEntity.status(httpStatus).body(errorDto)
    }
}