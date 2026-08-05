package io.koraframework.kotlin.example.helloworld

import io.koraframework.common.annotation.Component
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

@Component
@HttpController
class HelloWorldController {

    @Json
    data class HelloWorldResponse(val greeting: String)

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/hello/world/json")
    fun helloWorldJson(): HelloWorldResponse {
        return HelloWorldResponse("Hello World")
    }

    @Json
    @HttpRoute(method = HttpMethod.GET, path = "/hello/world/json/entity")
    fun helloWorldJsonEntity(): HttpResponseEntity<HelloWorldResponse> {
        return HttpResponseEntity.of(200, HelloWorldResponse("Hello World"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/hello/world")
    fun helloWorld(): HttpServerResponse {
        return HttpServerResponse.of(200, HttpBody.plaintext("Hello World"))
    }
}