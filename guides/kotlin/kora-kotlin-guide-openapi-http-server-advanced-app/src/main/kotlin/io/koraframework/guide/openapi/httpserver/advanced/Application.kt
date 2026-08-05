package io.koraframework.guide.openapi.httpserver.advanced

import io.koraframework.application.graph.KoraApplication
import io.koraframework.common.annotation.KoraApp
import io.koraframework.common.Principal
import io.koraframework.common.annotation.Tag
import io.koraframework.config.hocon.HoconConfigModule
import io.koraframework.guide.openapi.httpserver.advanced.controller.DataApiAuthConfig
import io.koraframework.guide.openapi.httpserver.advanced.controller.DataApiPrincipal
import io.koraframework.guide.openapi.httpserver.data.api.ApiSecurity
import io.koraframework.guide.openapi.httpserver.data.model.ErrorResponseTO
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.auth.HttpServerPrincipalExtractor
import io.koraframework.http.server.undertow.UndertowPublicHttpServerModule
import io.koraframework.json.common.JsonNullable
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.JsonModule
import io.koraframework.logging.logback.LogbackModule
import io.koraframework.openapi.management.OpenApiManagementModule
import io.koraframework.validation.module.ValidationModule
import io.koraframework.validation.module.http.server.ViolationExceptionHttpServerResponseMapper

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowPublicHttpServerModule,
    JsonModule,
    LogbackModule,
    ValidationModule,
    OpenApiManagementModule {

    fun customViolationExceptionHttpServerResponseMapper(
        errorResponseJsonWriter: JsonWriter<ErrorResponseTO>
    ): ViolationExceptionHttpServerResponseMapper {
        return ViolationExceptionHttpServerResponseMapper { _, exception ->
            val details = exception.violations.map { violation ->
                "Path ${violation.path()} violated: ${violation.message()}"
            }
            // an optional array of the specification becomes JsonNullable<T> in 2.0
            val response = ErrorResponseTO("Encountered '${details.size}' validation violations", JsonNullable.of(details))
            HttpServerResponse.of(400, HttpBody.json(errorResponseJsonWriter.toByteArray(response)))
        }
    }

    // В Kora 2.0 генератор именует теги по порядку security requirement в спецификации,
    // а не по имени схемы
    @Tag(ApiSecurity.SecurityRequirementTag0::class)
    fun apiKeyHttpServerPrincipalExtractor(config: DataApiAuthConfig): HttpServerPrincipalExtractor<String, Principal> {
        return HttpServerPrincipalExtractor { _, value ->
            if (value == null || config.value() != value) {
                throw SecurityException("Invalid API key")
            }
            DataApiPrincipal("data-api-client")
        }
    }
}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}
