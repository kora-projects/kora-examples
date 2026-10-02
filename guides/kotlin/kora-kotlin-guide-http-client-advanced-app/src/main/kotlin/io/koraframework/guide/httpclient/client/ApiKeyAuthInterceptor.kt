package io.koraframework.guide.httpclient.client

import io.koraframework.common.annotation.Component
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.response.HttpClientResponse

@Component
class ApiKeyAuthInterceptor(
    private val config: ApiKeyAuthConfig
) : HttpClientInterceptor {

    override fun processRequest(chain: HttpClientInterceptor.InterceptChain, request: HttpClientRequest): HttpClientResponse {
        val authorizedRequest = request.toBuilder()
            .header("Authorization", config.value())
            .build()
        return chain.process(authorizedRequest)
    }
}
