package io.koraframework.kotlin.example.http.client

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.InterceptWith

@InterceptWith(InterceptedHttpClient.ClientInterceptor::class)
@HttpClient("httpClient.default")
interface InterceptedHttpClient {

    @Component
    class ClientInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(ClientInterceptor::class.java)

        override fun processRequest(chain: HttpClientInterceptor.InterceptChain, request: HttpClientRequest): HttpClientResponse {
            logger.info("Client Level Interceptor")
            return chain.process(request)
        }
    }

    @Component
    class MethodInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(MethodInterceptor::class.java)

        override fun processRequest(chain: HttpClientInterceptor.InterceptChain, request: HttpClientRequest): HttpClientResponse {
            logger.info("Method Level Interceptor")
            return chain.process(request)
        }
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpResponseEntity<String>
}
