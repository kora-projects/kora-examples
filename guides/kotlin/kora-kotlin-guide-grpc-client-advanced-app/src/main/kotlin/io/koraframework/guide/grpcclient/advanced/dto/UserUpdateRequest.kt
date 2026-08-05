package io.koraframework.guide.grpcclient.advanced.dto

import io.koraframework.json.common.annotation.Json

@Json
data class UserUpdateRequest(val userId: String, val name: String, val email: String)
