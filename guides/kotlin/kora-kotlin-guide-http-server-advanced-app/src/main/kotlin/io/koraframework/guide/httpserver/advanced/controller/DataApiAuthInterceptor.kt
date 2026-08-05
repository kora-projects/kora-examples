package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import java.util.concurrent.CompletionStage

@Component
class DataApiAuthInterceptor(
    private val config: DataApiAuthConfig
) : HttpServerInterceptor {

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        val authorization = request.headers().getFirst("authorization")
        if (config.value() != authorization) {
            throw SecurityException("Invalid API key")
        }
        return chain.process(context, request)
    }
}
