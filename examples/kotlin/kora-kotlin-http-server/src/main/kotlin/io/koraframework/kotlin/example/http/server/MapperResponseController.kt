package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseMapper

@Component
@HttpController
class MapperResponseController {
    data class HelloWorldResponse(val greeting: String, val name: String)

    @Component
    class HelloWorldResponseMapper : HttpServerResponseMapper<HelloWorldResponse> {
        // the contract declares the result as @Nullable, so Kotlin must accept null here
        override fun apply(request: HttpServerRequest, result: HelloWorldResponse?): HttpServerResponse {
            requireNotNull(result)
            return HttpServerResponse.of(200, HttpBody.plaintext("${result.greeting} - ${result.name}"))
        }
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/response/{name}")
    @Mapping(HelloWorldResponseMapper::class)
    fun get(@Path name: String): HelloWorldResponse = HelloWorldResponse("Hello World", name)
}
