package ru.tinkoff.kora.guide.openapi.httpserver.advanced

import ru.tinkoff.kora.application.graph.KoraApplication
import ru.tinkoff.kora.common.KoraApp
import ru.tinkoff.kora.common.Principal
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.config.hocon.HoconConfigModule
import ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller.DataApiAuthConfig
import ru.tinkoff.kora.guide.openapi.httpserver.advanced.controller.DataApiPrincipal
import ru.tinkoff.kora.guide.openapi.httpserver.data.api.ApiSecurity
import ru.tinkoff.kora.guide.openapi.httpserver.data.model.ErrorResponseTO
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.auth.HttpServerPrincipalExtractor
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.module.JsonModule
import ru.tinkoff.kora.logging.logback.LogbackModule
import ru.tinkoff.kora.openapi.management.OpenApiManagementModule
import ru.tinkoff.kora.validation.module.ValidationModule
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper
import java.util.concurrent.CompletableFuture

@KoraApp
interface Application :
    HoconConfigModule,
    UndertowHttpServerModule,
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
            val response = ErrorResponseTO("Encountered '${details.size}' validation violations", details)
            HttpServerResponse.of(400, HttpBody.json(errorResponseJsonWriter.toByteArrayUnchecked(response)))
        }
    }

    @Tag(ApiSecurity.ApiKeyAuth::class)
    fun apiKeyHttpServerPrincipalExtractor(config: DataApiAuthConfig): HttpServerPrincipalExtractor<Principal> {
        return HttpServerPrincipalExtractor { _, value ->
            if (value == null || config.value() != value) {
                throw SecurityException("Invalid API key")
            }
            CompletableFuture.completedFuture(DataApiPrincipal("data-api-client"))
        }
    }

}

fun main() {
    KoraApplication.run(ApplicationGraph::graph)
}
