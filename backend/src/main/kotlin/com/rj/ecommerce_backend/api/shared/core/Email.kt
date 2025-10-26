package com.rj.ecommerce_backend.api.shared.core

import jakarta.validation.constraints.Email as EmailFormat // Alias to avoid conflict with class name
import jakarta.validation.constraints.NotBlank
import jakarta.persistence.Embeddable

@Embeddable
data class Email(
    @field:NotBlank(message = "Email value cannot be blank.")
    @field:EmailFormat(message = "Email should be in a valid format.") // Uses Jakarta Bean Validation's @Email
    val value: String
) {
    // The init block is now less critical for these specific validations
    // if Bean Validation is active where Email objects are created/validated.
    // You could still keep it for an extra layer of defense or for validations
    // not covered by standard annotations.
    // init {
    //     // Basic checks still useful as a fallback or for non-bean-validated contexts
    //     require(value.isNotBlank()) { "Email value cannot be blank." }
    // }

    companion object {
        @JvmStatic
        fun of(emailValue: String): Email {
            // When creating through this factory, if you want immediate validation feedback
            // before a Bean Validation framework might pick it up, you could manually validate here
            // or rely on the annotations if instances are typically validated by a framework.
            // For simplicity, let's assume the constructor + annotations are primary.
            val email = Email(emailValue)
            // If you wanted to trigger bean validation programmatically here (more advanced):
            // val validator = Validation.buildDefaultValidatorFactory().validator
            // val violations = validator.validate(email)
            // if (violations.isNotEmpty()) {
            //     throw ConstraintViolationException(violations)
            // }
            return email
        }
    }
}
