package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.Header
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.validation.common.annotation.Pattern
import io.koraframework.validation.common.annotation.Size
import io.koraframework.validation.common.annotation.Validate

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
