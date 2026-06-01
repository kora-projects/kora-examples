package ru.tinkoff.kora.guide.grpcclient.advanced.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class UserUpdateRequest(val userId: String, val name: String, val email: String)
