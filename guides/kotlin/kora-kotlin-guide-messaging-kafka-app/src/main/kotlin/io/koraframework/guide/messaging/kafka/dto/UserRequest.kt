package io.koraframework.guide.messaging.kafka.dto

import io.koraframework.json.common.annotation.Json

@Json
data class UserRequest(val name: String, val email: String)
