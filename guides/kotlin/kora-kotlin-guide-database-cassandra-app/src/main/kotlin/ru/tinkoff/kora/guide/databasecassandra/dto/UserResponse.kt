package ru.tinkoff.kora.guide.databasecassandra.dto

import ru.tinkoff.kora.json.common.annotation.Json
import java.time.Instant

@Json
data class UserResponse(val id: String, val name: String, val email: String, val createdAt: Instant)
