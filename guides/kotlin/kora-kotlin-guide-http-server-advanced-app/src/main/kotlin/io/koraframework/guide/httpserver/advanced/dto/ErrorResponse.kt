package io.koraframework.guide.httpserver.advanced.dto

import io.koraframework.json.common.annotation.Json

@Json
data class ErrorResponse(
    val message: String
)
