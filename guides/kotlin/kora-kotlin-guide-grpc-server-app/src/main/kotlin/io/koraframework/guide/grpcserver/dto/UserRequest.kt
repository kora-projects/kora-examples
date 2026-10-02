package io.koraframework.guide.grpcserver.dto

import io.koraframework.json.common.annotation.Json

@Json
data class UserRequest(val name: String, val email: String)
