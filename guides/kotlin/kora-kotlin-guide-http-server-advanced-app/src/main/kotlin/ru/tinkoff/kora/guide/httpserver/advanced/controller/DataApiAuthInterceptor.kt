package ru.tinkoff.kora.guide.httpserver.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.http.server.common.HttpServerInterceptor
import ru.tinkoff.kora.http.server.common.HttpServerRequest
import ru.tinkoff.kora.http.server.common.HttpServerResponse
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
