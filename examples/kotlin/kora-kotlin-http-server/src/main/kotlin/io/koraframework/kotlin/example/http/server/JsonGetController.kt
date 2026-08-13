package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

@Component
@HttpController
class JsonGetController {
    @Json
    data class HelloWorldResponse(val greeting: String)

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json")
    fun get(): HelloWorldResponse = HelloWorldResponse("Hello world")

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/json/entity")
    fun getEntity(): HttpResponseEntity<HelloWorldResponse> =
        HttpResponseEntity.of(201, HelloWorldResponse("Hello world"))
}
