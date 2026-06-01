package ru.tinkoff.kora.guide.httpclient.client

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse
import java.util.concurrent.CompletionStage

@Component
class ApiKeyAuthInterceptor(
    private val config: ApiKeyAuthConfig
) : HttpClientInterceptor {

    override fun processRequest(
        ctx: Context,
        chain: HttpClientInterceptor.InterceptChain,
        request: HttpClientRequest
    ): CompletionStage<HttpClientResponse> {
        val authorizedRequest = request.toBuilder()
            .header("Authorization", config.value())
            .build()
        return chain.process(ctx, authorizedRequest)
    }
}
