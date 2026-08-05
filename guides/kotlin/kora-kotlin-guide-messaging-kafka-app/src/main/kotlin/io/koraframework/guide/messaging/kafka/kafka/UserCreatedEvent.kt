package io.koraframework.guide.messaging.kafka.kafka

import io.koraframework.json.common.annotation.Json
import java.time.LocalDateTime

@Json
data class UserCreatedEvent(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)
