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
open class ValidateParametersController {
    @Validate
    @HttpRoute(method = HttpMethod.GET, path = "/validate/{path}")
    open fun get(
        @Path path: String,
        @Pattern("q.+") @Query query: String,
        @Size(min = 2, max = Int.MAX_VALUE) @Query("Queries") queries: List<String>,
        @Pattern("h.+") @Header header: String,
        @Size(min = 2, max = Int.MAX_VALUE) @Header("Headers") headers: List<String>
    ): HttpServerResponse {
        val body = "Path: $path, Query: $query, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpHeaders.of("headerName", "headerValue"), HttpBody.plaintext(body))
    }
}

