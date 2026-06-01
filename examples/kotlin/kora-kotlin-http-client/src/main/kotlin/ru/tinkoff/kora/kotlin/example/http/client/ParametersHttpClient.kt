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

@HttpClient(configPath = "httpClient.default")
interface ParametersHttpClient {
    @HttpRoute(method = HttpMethod.POST, path = "/parameters/{path}")
    fun post(
        @Path path: String,
        @Nullable @Query query: String?,
        @Nullable @Query("queries") queries: List<String>?,
        @Nullable @Header header: String?,
        @Nullable @Header("headers") headers: List<String>?
    ): HttpResponseEntity<String>
}

