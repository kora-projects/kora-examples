package ru.tinkoff.kora.kotlin.example.helloworld

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.server.common.HttpServerResponse
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

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