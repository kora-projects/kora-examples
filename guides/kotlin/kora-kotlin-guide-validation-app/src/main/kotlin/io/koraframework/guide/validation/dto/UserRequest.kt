package io.koraframework.guide.validation.dto

import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.NotBlank
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size

@Json
data class UserRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val name: String,
    @field:NotBlank
    @field:Pattern("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    val email: String
)
