package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse

@Component
class DataApiAuthInterceptor(
    private val config: DataApiAuthConfig
) : HttpServerInterceptor {

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
        val authorization = request.headers().getFirst("authorization")
        if (config.value() != authorization) {
            throw SecurityException("Invalid API key")
        }
        return chain.process(request)
    }
}
