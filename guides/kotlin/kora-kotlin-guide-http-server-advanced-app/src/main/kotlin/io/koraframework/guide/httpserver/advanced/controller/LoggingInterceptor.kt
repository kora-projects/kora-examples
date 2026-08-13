package io.koraframework.guide.httpserver.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.http.server.common.interceptor.HttpServerInterceptor
import io.koraframework.http.server.common.request.HttpServerRequest
import io.koraframework.http.server.common.response.HttpServerResponse

@Component
class LoggingInterceptor : HttpServerInterceptor {

    override fun intercept(request: HttpServerRequest, chain: HttpServerInterceptor.InterceptChain): HttpServerResponse {
        val started = System.nanoTime()
        // stays 500 when the chain throws, matching the reactive version that logged 500 on failure
        var statusCode = 500
        try {
            val response = chain.process(request)
            statusCode = response.code()
            return response
        } finally {
            val durationMs = (System.nanoTime() - started) / 1_000_000
            println("Request: ${request.method()} ${request.path()} -> $statusCode ($durationMs ms)")
        }
    }
}
