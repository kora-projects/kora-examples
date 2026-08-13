package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse

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
