package ru.tinkoff.kora.guide.messaging.kafka.dto

import ru.tinkoff.kora.json.common.annotation.Json

@Json
data class UserAcceptedResponse(val id: String)
