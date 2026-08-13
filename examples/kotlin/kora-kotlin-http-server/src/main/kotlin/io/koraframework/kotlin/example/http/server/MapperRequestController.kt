package io.koraframework.kotlin.example.http.server

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Mapping
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.request.HttpServerRequestMapper
import io.koraframework.http.server.common.response.HttpServerResponse

@Component
@HttpController
class MapperRequestController {
    data class UserContext(val userId: String?, val traceId: String?)

    @Component
    class UserContextRequestMapper : HttpServerRequestMapper<UserContext> {
        override fun apply(request: HttpServerRequest): UserContext =
            UserContext(request.headers().getFirst("x-user-id"), request.headers().getFirst("x-trace-id"))
    }

    @HttpRoute(method = HttpMethod.GET, path = "/mapper/request")
    @Mapping(UserContextRequestMapper::class)
    fun get(@Mapping(UserContextRequestMapper::class) context: UserContext): HttpServerResponse =
        HttpServerResponse.of(200, HttpBody.plaintext("${context.userId}:${context.traceId}"))
}
