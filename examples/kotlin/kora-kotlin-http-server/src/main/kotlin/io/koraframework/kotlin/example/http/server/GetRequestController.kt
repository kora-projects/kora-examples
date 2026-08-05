package io.koraframework.kotlin.example.http.server

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Tag
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.*
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequestMapper
import io.koraframework.http.server.common.response.HttpServerResponseMapper
import io.koraframework.json.common.JsonWriter
import io.koraframework.json.common.annotation.Json
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size
import io.koraframework.validation.common.annotation.Validate
import java.util.concurrent.CompletionStage

@Component
@HttpController
class GetRequestController {
    @HttpRoute(method = HttpMethod.GET, path = "/request")
    fun get(request: HttpServerRequest): HttpServerResponse {
        val queries = request.queryParams()["Queries"]
        val queryValue = request.queryParams()["query"]?.stream()?.findFirst()?.orElse(null)
        val header = request.headers().getFirst("header")
        val headers = request.headers().getAll("Headers")
        val body = "Path: ${request.path()}, Query: $queryValue, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpBody.plaintext(body))
    }
}

