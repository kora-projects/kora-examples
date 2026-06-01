package ru.tinkoff.kora.guide.openapi.httpserver.dto

import java.time.LocalDateTime

data class UserResponse(val id: String, val name: String, val email: String, val createdAt: LocalDateTime)
