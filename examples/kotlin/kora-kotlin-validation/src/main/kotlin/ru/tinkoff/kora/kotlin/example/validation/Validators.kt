package ru.tinkoff.kora.kotlin.example.validation

import jakarta.annotation.Nullable
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.validation.common.annotation.*
import java.util.*

@Root
@Component
open class ArgumentValidator {
    @Validate
    open fun calculate(
        @Valid user: ArgumentUser,
        @Range(from = 1.0, to = 900.0) weight: Int,
        @Pattern("ME\\d+") code: String
    ): Int = code.substring(2).toInt()
}

@Valid
data class ArgumentUser(
    @field:NotBlank val id: String,
    @field:Size(min = 3, max = 6) val name: String,
    @field:Nullable val status: String?
)

@Root
@Component
open class ResultValidator {
    @Valid
    @Validate
    open fun create(name: String, status: String?): ResultUser = ResultUser(UUID.randomUUID().toString(), name, status)
}

@Valid
data class ResultUser(
    @field:NotBlank val id: String,
    @field:Size(min = 3, max = 6) val name: String,
    @field:Nullable @field:Size(min = 1, max = 10) val status: String?
)
