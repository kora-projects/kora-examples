package io.koraframework.guide.validation

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.annotation.Tag
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.guide.validation.dto.ValidationErrorDetails
import io.koraframework.guide.validation.dto.ValidationErrorResponse
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.HttpServer
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.validation.common.Violation
import io.koraframework.validation.module.ValidationModule
import io.koraframework.validation.module.http.server.ValidationHttpServerInterceptor
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper

@KoraApp
interface Application :
    HoconConfigModule,
    JsonModule,
    LogbackModule,
    ValidationModule,
    UndertowPublicHttpServerModule {

    fun violationExceptionHttpServerResponseMapper(
        errorResponseJsonWriter: JsonWriter<ValidationErrorResponse>
    ): ViolationExceptionHttpServerResponseMapper {
        return ViolationExceptionHttpServerResponseMapper { _, exception ->
            HttpServerResponse.of(
                400,
                HttpBody.json(
                    errorResponseJsonWriter.toByteArray(
                        ValidationErrorResponse.of(toValidationErrors(exception.violations))
                    )
                )
            )
        }
    }

    @Tag(HttpServer::class)
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
