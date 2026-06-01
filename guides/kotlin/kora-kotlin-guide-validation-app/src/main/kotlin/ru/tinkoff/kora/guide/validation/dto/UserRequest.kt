package ru.tinkoff.kora.guide.validation.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.NotBlank
import ru.tinkoff.kora.validation.common.annotation.Pattern
import ru.tinkoff.kora.validation.common.annotation.Size

@Json
data class UserRequest(
    @field:NotBlank
    @field:Size(min = 2, max = 100)
    val name: String,
    @field:NotBlank
    @field:Pattern("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")
    val email: String
)
