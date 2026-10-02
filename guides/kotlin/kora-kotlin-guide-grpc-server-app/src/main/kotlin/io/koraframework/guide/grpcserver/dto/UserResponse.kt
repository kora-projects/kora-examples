package io.koraframework.guide.grpcserver.dto

import io.koraframework.json.common.annotation.Json
import java.time.LocalDateTime

@Json
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)
