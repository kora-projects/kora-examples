package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.response.HttpServerResponse

@Component
@HttpController
class SyncController {
    @HttpRoute(method = HttpMethod.GET, path = "/sync")
    fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}
