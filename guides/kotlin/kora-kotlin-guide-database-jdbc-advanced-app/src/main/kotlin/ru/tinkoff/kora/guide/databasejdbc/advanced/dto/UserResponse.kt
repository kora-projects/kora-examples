package ru.tinkoff.kora.guide.databasejdbc.advanced.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.LocalDateTime

@Json
data class UserResponse(
    val id: String,
    val name: String,
    val email: String,
    val createdAt: LocalDateTime
)
