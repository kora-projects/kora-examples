package ru.tinkoff.kora.guide.httpserver.advanced.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class ErrorResponse(
    val message: String
)
