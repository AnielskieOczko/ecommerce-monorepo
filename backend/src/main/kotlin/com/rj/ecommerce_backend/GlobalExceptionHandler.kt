package com.rj.ecommerce_backend

import com.rj.ecommerce_backend.api.shared.core.ProblemDetailDTO
import com.rj.ecommerce_backend.order.exception.AccessDeniedException
import com.rj.ecommerce_backend.order.exception.OrderCancellationException
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.exception.OrderServiceException
import com.rj.ecommerce_backend.payment.exception.PaymentProcessingException
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.user.exception.EmailAlreadyExistsException
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import com.stripe.exception.StripeException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.net.URI

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler {

    // --- Specific Business Logic Exceptions ---

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.warn(ex) { "Conflict due to existing email: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.CONFLICT, "Email In Use", ex.message, request)
    }

    @ExceptionHandler(UserNotFoundException::class, ProductNotFoundException::class, OrderNotFoundException::class, CategoryNotFoundException::class)
    fun handleNotFoundExceptions(ex: RuntimeException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.warn { "Resource not found: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.message, request)
    }

    @ExceptionHandler(InsufficientStockException::class, OrderCancellationException::class)
    fun handleBadRequestExceptions(ex: RuntimeException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.warn { "Bad request due to business rule violation: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.BAD_REQUEST, "Invalid Request", ex.message, request)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.warn(ex) { "Access denied: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.FORBIDDEN, "Access Denied", "You do not have permission to access this resource.", request)
    }

    // --- MERGED PAYMENT EXCEPTIONS ---

    @ExceptionHandler(PaymentProcessingException::class)
    fun handlePaymentProcessingException(ex: PaymentProcessingException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.error(ex) { "A payment processing error occurred: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.BAD_REQUEST, "Payment Processing Error", ex.message, request)
    }

    @ExceptionHandler(StripeException::class)
    fun handleStripeException(ex: StripeException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        val httpStatus = HttpStatus.resolve(ex.statusCode) ?: HttpStatus.INTERNAL_SERVER_ERROR
        logger.error(ex) { "An error occurred while communicating with Stripe: ${ex.message}" }
        return buildProblemDetailResponse(httpStatus, "Payment Provider Error", "An external error occurred with the payment provider.", request)
    }

    // --- Validation Error Handler (Already Correct) ---

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        val validationErrors = ex.bindingResult.allErrors.associate { error ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.defaultMessage ?: "Invalid value"
            fieldName to errorMessage
        }

        logger.warn { "Validation failed for request to ${request.getDescription(false)}: $validationErrors" }

        val problemDetail = ProblemDetailDTO(
            type = URI.create("/errors/validation-failed"),
            title = "Validation Failed",
            status = HttpStatus.BAD_REQUEST.value(),
            detail = "One or more fields are invalid. Please check the 'details' object for more information.",
            instance = URI.create(request.getDescription(false)),
            details = validationErrors
        )

        return ResponseEntity.badRequest().body(problemDetail)
    }

    // --- Generic and Service-Level Exceptions ---

    @ExceptionHandler(OrderServiceException::class)
    fun handleOrderServiceException(ex: OrderServiceException, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.error(ex) { "A critical error occurred in the order service: ${ex.message}" }
        return buildProblemDetailResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Service Error", "An error occurred while processing the order.", request)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        logger.error(ex) { "An unexpected error occurred for request: ${request.getDescription(false)}" }
        return buildProblemDetailResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred. Please try again later.",
            request
        )
    }

    // --- Reusable Helper Function for ProblemDetailDTO ---

    private fun buildProblemDetailResponse(status: HttpStatus, title: String, detail: String?, request: WebRequest): ResponseEntity<ProblemDetailDTO> {
        val problemDetail = ProblemDetailDTO(
            title = title,
            status = status.value(),
            detail = detail,
            instance = URI.create(request.getDescription(false))
        )
        return ResponseEntity(problemDetail, status)
    }
}