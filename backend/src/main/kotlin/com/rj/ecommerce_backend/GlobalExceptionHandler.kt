package com.rj.ecommerce_backend

import com.rj.ecommerce.api.shared.core.ErrorDTO
import com.rj.ecommerce.api.shared.core.ProblemDetailDTO
import com.rj.ecommerce_backend.order.exception.AccessDeniedException
import com.rj.ecommerce_backend.order.exception.OrderCancellationException
import com.rj.ecommerce_backend.order.exception.OrderNotFoundException
import com.rj.ecommerce_backend.order.exception.OrderServiceException
import com.rj.ecommerce_backend.product.exception.CategoryNotFoundException
import com.rj.ecommerce_backend.product.exception.InsufficientStockException
import com.rj.ecommerce_backend.product.exception.ProductNotFoundException
import com.rj.ecommerce_backend.user.exception.EmailAlreadyExistsException
import com.rj.ecommerce_backend.user.exception.UserNotFoundException
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import java.net.URI
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@ControllerAdvice
class GlobalExceptionHandler {

    // --- Specific Business Logic Exceptions ---

    @ExceptionHandler(EmailAlreadyExistsException::class)
    fun handleEmailAlreadyExistsException(ex: EmailAlreadyExistsException): ResponseEntity<ErrorDTO> {
        logger.warn(ex) { "Conflict due to existing email: ${ex.message}" }
        return buildErrorResponse(HttpStatus.CONFLICT, ex.message)
    }

    @ExceptionHandler(UserNotFoundException::class, ProductNotFoundException::class, OrderNotFoundException::class, CategoryNotFoundException::class)
    fun handleNotFoundExceptions(ex: RuntimeException): ResponseEntity<ErrorDTO> {
        logger.warn { "Resource not found: ${ex.message}" }
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.message)
    }

    @ExceptionHandler(InsufficientStockException::class, OrderCancellationException::class)
    fun handleBadRequestExceptions(ex: RuntimeException): ResponseEntity<ErrorDTO> {
        logger.warn { "Bad request due to business rule violation: ${ex.message}" }
        return buildErrorResponse(HttpStatus.BAD_REQUEST, ex.message)
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(ex: AccessDeniedException): ResponseEntity<ErrorDTO> {
        logger.warn(ex) { "Access denied: ${ex.message}" }
        return buildErrorResponse(HttpStatus.FORBIDDEN, "You do not have permission to access this resource.")
    }

    // --- Validation Error Handler (Using RFC 7807 ProblemDetail) ---

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
    fun handleOrderServiceException(ex: OrderServiceException): ResponseEntity<ErrorDTO> {
        logger.error(ex) { "A critical error occurred in the order service: ${ex.message}" }
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An error occurred while processing the order.")
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(ex: Exception, request: WebRequest): ResponseEntity<ErrorDTO> {
        logger.error(ex) { "An unexpected error occurred for request: ${request.getDescription(false)}" }
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred. Please try again later."
        )
    }

    // --- Helper Function ---

    private fun buildErrorResponse(status: HttpStatus, message: String?): ResponseEntity<ErrorDTO> {
        val errorDTO = ErrorDTO(
            status = status.value(),
            message = message,
            timestamp = LocalDateTime.now()
        )
        return ResponseEntity(errorDTO, status)
    }
}