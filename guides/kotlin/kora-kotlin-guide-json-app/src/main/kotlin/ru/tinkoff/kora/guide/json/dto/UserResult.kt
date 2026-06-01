package ru.tinkoff.kora.guide.json.dto

import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorField
import ru.tinkoff.kora.json.common.annotation.JsonDiscriminatorValue

@Json
@JsonDiscriminatorField("status")
sealed interface UserResult {

    @Json
    enum class Status {
        OK,
        ERROR
    }

    val status: Status

    @Json
    @JsonDiscriminatorValue("OK")
    data class UserSuccess(
        override val status: Status,
        val user: UserResponse
    ) : UserResult

    @Json
    @JsonDiscriminatorValue("ERROR")
    data class UserError(
        override val status: Status,
        val message: String
    ) : UserResult
}
