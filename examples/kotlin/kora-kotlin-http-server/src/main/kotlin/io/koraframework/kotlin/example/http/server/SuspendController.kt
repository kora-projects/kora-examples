package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.response.HttpServerResponse

/**
 * Kora 2.0 handlers are synchronous and run on a Virtual Thread. A `suspend` route is still accepted:
 * the generated handler bridges it with `runBlocking`, so the coroutine runs to completion on that
 * same Virtual Thread rather than being dispatched elsewhere.
 */
@Component
@HttpController
class SuspendController {
    @HttpRoute(method = HttpMethod.GET, path = "/suspend")
    suspend fun get(): HttpServerResponse = HttpServerResponse.of(200, HttpBody.plaintext("Hello world"))
}
