package io.koraframework.guide.validation.dto

import io.koraframework.json.common.annotation.Json

@Json
data class ValidationErrorDetails(
    val field: String,
    val message: String
)
