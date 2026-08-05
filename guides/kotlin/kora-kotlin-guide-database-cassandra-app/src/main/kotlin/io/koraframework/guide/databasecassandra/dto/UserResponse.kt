package io.koraframework.guide.databasecassandra.dto

import io.koraframework.json.common.annotation.Json
import java.time.Instant

@Json
data class UserResponse(val id: String, val name: String, val email: String, val createdAt: Instant)
