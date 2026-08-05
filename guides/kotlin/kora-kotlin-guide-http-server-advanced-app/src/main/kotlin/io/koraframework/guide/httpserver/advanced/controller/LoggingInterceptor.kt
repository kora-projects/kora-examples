package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse
import java.util.concurrent.CompletionStage

@Component
class LoggingInterceptor : HttpServerInterceptor {

    override fun intercept(
        context: Context,
        request: HttpServerRequest,
        chain: HttpServerInterceptor.InterceptChain
    ): CompletionStage<HttpServerResponse> {
        val started = System.nanoTime()
        return chain.process(context, request).whenComplete { response, _ ->
            val durationMs = (System.nanoTime() - started) / 1_000_000
            val statusCode = response?.code() ?: 500
            println("Request: ${request.method()} ${request.path()} -> $statusCode (${durationMs} ms)")
        }
    }
}
