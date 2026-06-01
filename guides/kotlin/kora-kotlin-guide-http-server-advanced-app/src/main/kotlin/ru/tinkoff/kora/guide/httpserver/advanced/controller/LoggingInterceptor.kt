package ru.tinkoff.kora.guide.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
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
