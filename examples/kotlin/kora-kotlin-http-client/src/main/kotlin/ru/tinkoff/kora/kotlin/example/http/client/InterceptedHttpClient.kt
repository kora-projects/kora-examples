package ru.tinkoff.kora.kotlin.example.http.client

import jakarta.annotation.Nullable
import org.slf4j.LoggerFactory
import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.common.Context
import ru.tinkoff.kora.common.Mapping
import ru.tinkoff.kora.common.annotation.Root
import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper
import ru.tinkoff.kora.http.client.common.annotation.ResponseCodeMapper.DEFAULT
import ru.tinkoff.kora.http.client.common.interceptor.HttpClientInterceptor
import ru.tinkoff.kora.http.client.common.request.HttpClientRequest
import ru.tinkoff.kora.http.client.common.request.HttpClientRequestMapper
import ru.tinkoff.kora.http.client.common.response.HttpClientResponse
import ru.tinkoff.kora.http.client.common.response.HttpClientResponseMapper
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.*
import ru.tinkoff.kora.http.common.body.HttpBody
import ru.tinkoff.kora.http.common.body.HttpBodyOutput
import ru.tinkoff.kora.http.common.form.FormMultipart
import ru.tinkoff.kora.http.common.form.FormUrlEncoded
import ru.tinkoff.kora.json.common.annotation.Json
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletionStage

@InterceptWith(InterceptedHttpClient.ClientInterceptor::class)
@HttpClient(configPath = "httpClient.default")
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

