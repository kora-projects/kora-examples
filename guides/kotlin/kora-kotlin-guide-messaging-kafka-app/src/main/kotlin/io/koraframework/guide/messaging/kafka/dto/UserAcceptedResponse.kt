package io.koraframework.guide.messaging.kafka.dto

import io.koraframework.json.common.annotation.Json

@Json
data class UserAcceptedResponse(val id: String)
