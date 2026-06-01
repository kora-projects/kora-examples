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
class MapperResponseController {
    data class HelloWorldResponse(val greeting: String, val name: String)

    class HelloWorldResponseMapper : HttpServerResponseMapper<HelloWorldResponse> {
        override fun apply(ctx: Context, request: HttpServerRequest, result: HelloWorldResponse): HttpServerResponse =
            HttpServerResponse.of(200, HttpBody.plaintext("${result.greeting} - ${result.name}"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/response/{name}")
    @Mapping(HelloWorldResponseMapper::class)
    fun get(@Path name: String): HelloWorldResponse = HelloWorldResponse("Hello World", name)
}

