package ru.tinkoff.kora.guide.validation.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class ValidationErrorResponse(
    val code: String,
    val message: String,
    val errors: List<ValidationErrorDetails>
) {
    companion object {
        fun of(errors: List<ValidationErrorDetails>): ValidationErrorResponse {
            return ValidationErrorResponse(
                code = "VALIDATION_ERROR",
                message = "Validation failed",
                errors = errors
            )
        }
    }
}
