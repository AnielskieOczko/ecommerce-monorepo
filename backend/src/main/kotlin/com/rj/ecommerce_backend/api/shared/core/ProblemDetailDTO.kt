package com.rj.ecommerce_backend.api.shared.core

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import java.net.URI
import java.time.Instant

@Schema(description = "A standard error response format compliant with RFC 7807 Problem Details for HTTP APIs.")
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ProblemDetailDTO(
    @field:Schema(
        description = "A URI identifier that categorizes the problem type.",
        example = "/errors/validation-failed"
    )
    val type: URI = URI.create("/errors/uncategorized"),

    @field:Schema(
        description = "A short, human-readable summary of the problem type.",
        example = "Validation Failed"
    )
    val title: String,

    @field:Schema(
        description = "The HTTP status code for this occurrence of the problem.",
        example = "400"
    )
    val status: Int,

    @field:Schema(
        description = "A human-readable explanation specific to this occurrence of the problem.",
        example = "One or more fields are invalid."
    )
    val detail: String?,

    @field:Schema(
        description = "A URI reference that identifies the specific occurrence of the problem.",
        example = "/api/v1/users"
    )
    val instance: URI?,

    @field:Schema(
        description = "The timestamp of when the error occurred."
    )
    val timestamp: Instant = Instant.now(),

    @field:Schema(
        description = "An object containing validation errors or other specific details."
    )
    val details: Map<String, Any>? = null
)