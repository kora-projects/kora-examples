package ru.tinkoff.kora.guide.validation.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class ValidationErrorDetails(
    val field: String,
    val message: String
)
