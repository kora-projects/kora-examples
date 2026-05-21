package ru.tinkoff.kora.kotlin.example.http.server

import jakarta.annotation.Nullable
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.Tag
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.*
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.header.HttpHeaders
import ru.tinkoff.kora.http.server.common.*
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.http.server.common.handler.HttpServerRequestMapper
import ru.tinkoff.kora.http.server.common.handler.HttpServerResponseMapper
import ru.tinkoff.kora.json.common.JsonWriter
import ru.tinkoff.kora.json.common.annotation.Json
import ru.tinkoff.kora.validation.common.annotation.Pattern
import ru.tinkoff.kora.validation.common.annotation.Size
import ru.tinkoff.kora.validation.common.annotation.Validate
import java.util.concurrent.CompletionStage

@Component
@HttpController
class MapperRequestController {
    data class UserContext(val userId: String?, val traceId: String?)

    class UserContextRequestMapper : HttpServerRequestMapper<UserContext> {
        override fun apply(request: HttpServerRequest): UserContext =
            UserContext(request.headers().getFirst("x-user-id"), request.headers().getFirst("x-trace-id"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/request")
    @Mapping(UserContextRequestMapper::class)
    fun get(@Mapping(UserContextRequestMapper::class) context: UserContext): HttpServerResponse =
        HttpServerResponse.of(200, HttpBody.plaintext("${context.userId}:${context.traceId}"))
}

