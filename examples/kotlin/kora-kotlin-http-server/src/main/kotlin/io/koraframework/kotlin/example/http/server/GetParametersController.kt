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

@Component
@HttpController
class GetParametersController {
    @HttpRoute(method = HttpMethod.GET, path = "/parameters/{path}")
    fun get(
        @Path path: String,
        @Query query: String?,
        @Query("Queries") queries: List<String>?,
        @Header header: String?,
        @Header("Headers") headers: List<String>?
    ): HttpServerResponse {
        val body = "Path: $path, Query: $query, Queries: $queries, Header: $header, Headers: $headers"
        return HttpServerResponse.of(200, HttpHeaders.of("headerName", "headerValue"), HttpBody.plaintext(body))
    }
}
