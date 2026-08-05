package io.koraframework.guide.httpclient.client

import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.response.HttpClientResponse
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
