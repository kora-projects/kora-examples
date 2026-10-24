package io.koraframework.kotlin.example.validation

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Root
import io.koraframework.validation.common.annotation.*
import java.util.UUID

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
    @field:Size(min = 1, max = 10) val status: String?
)

