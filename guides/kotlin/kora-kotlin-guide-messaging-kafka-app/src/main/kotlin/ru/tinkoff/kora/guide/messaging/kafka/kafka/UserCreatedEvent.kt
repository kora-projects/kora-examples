package ru.tinkoff.kora.guide.messaging.kafka.kafka

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDateTime

@Json
data class UserCreatedEvent(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)
