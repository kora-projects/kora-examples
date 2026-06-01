package ru.tinkoff.kora.guide.grpcserver.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class UserRequest(val name: String, val email: String)
