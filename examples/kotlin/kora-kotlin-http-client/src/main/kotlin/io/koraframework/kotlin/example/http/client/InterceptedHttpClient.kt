package io.koraframework.kotlin.example.http.client

import org.slf4j.LoggerFactory
import io.koraframework.common.annotation.Component
import io.koraframework.common.Context
import io.koraframework.common.annotation.Mapping
import io.koraframework.common.annotation.Root
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.annotation.ResponseCodeMapper
import io.koraframework.http.client.common.annotation.ResponseCodeMapper.DEFAULT
import io.koraframework.http.client.common.interceptor.HttpClientInterceptor
import io.koraframework.http.client.common.request.HttpClientRequest
import io.koraframework.http.client.common.request.HttpClientRequestMapper
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.client.common.response.HttpClientResponseMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.body.HttpBodyOutput
import io.koraframework.http.common.form.FormMultipart
import io.koraframework.http.common.form.FormUrlEncoded
import io.koraframework.json.common.annotation.Json
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

@InterceptWith(InterceptedHttpClient.ClientInterceptor::class)
@HttpClient("httpClient.default")
interface InterceptedHttpClient {
    class ClientInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(ClientInterceptor::class.java)

        override fun processRequest(
            ctx: Context,
            chain: HttpClientInterceptor.InterceptChain,
            request: HttpClientRequest
        ): CompletionStage<HttpClientResponse> {
            logger.info("Client Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    class MethodInterceptor : HttpClientInterceptor {
        private val logger = LoggerFactory.getLogger(MethodInterceptor::class.java)

        override fun processRequest(
            ctx: Context,
            chain: HttpClientInterceptor.InterceptChain,
            request: HttpClientRequest
        ): CompletionStage<HttpClientResponse> {
            logger.info("Method Level Interceptor")
            return chain.process(ctx, request)
        }
    }

    @InterceptWith(MethodInterceptor::class)
    @HttpRoute(method = HttpMethod.GET, path = "/intercepted")
    fun get(): HttpResponseEntity<String>
}

