package ru.tinkoff.kora.guide.validation

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.guide.validation.dto.ValidationErrorDetails
import ru.tinkoff.kora.guide.validation.dto.ValidationErrorResponse
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerModule
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.validation.common.Violation
import ru.tinkoff.kora.validation.module.ValidationModule
import ru.tinkoff.kora.validation.module.http.server.ValidationHttpServerInterceptor
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    ValidationModule,
    UndertowHttpServerModule {

    fun violationExceptionHttpServerResponseMapper(
        errorResponseJsonWriter: JsonWriter<ValidationErrorResponse>
    ): ViolationExceptionHttpServerResponseMapper {
        return ViolationExceptionHttpServerResponseMapper { _, exception ->
            HttpServerResponse.of(
                400,
                HttpBody.json(
                    errorResponseJsonWriter.toByteArrayUnchecked(
                        ValidationErrorResponse.of(toValidationErrors(exception.violations))
                    )
                )
            )
        }
    }

    @Tag(HttpServerModule::class)
    override fun validationHttpServerInterceptor(
        violationExceptionHttpServerResponseMapper: ViolationExceptionHttpServerResponseMapper
    ): ValidationHttpServerInterceptor {
        return ValidationHttpServerInterceptor(violationExceptionHttpServerResponseMapper)
    }

    private fun toValidationErrors(violations: List<Violation>): List<ValidationErrorDetails> {
        return violations.map { violation ->
            ValidationErrorDetails(normalizeField(violation), violation.message())
        }
    }

    private fun normalizeField(violation: Violation): String {
        val fullPath = violation.path().full()
        val lastDot = fullPath.lastIndexOf('.')
        return if (lastDot >= 0) fullPath.substring(lastDot + 1) else fullPath
    }
}


fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}
