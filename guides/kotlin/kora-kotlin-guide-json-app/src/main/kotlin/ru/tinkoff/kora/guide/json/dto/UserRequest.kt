package ru.tinkoff.kora.guide.json.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class UserRequest(
    val name: String,
    val email: String
)
