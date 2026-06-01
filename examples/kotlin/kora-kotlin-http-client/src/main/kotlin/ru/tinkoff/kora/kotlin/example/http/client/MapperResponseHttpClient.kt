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
interface MapperResponseHttpClient {
    class ResponseSuccessMapper : HttpClientResponseMapper<UserResponse> {
        override fun apply(response: HttpClientResponse): UserResponse {
            response.body().asInputStream().use {
                val message = String(it.readAllBytes(), StandardCharsets.UTF_8)
                return UserResponse(UserResponse.Payload(message), null)
            }
        }
    }

    class ResponseErrorMapper : HttpClientResponseMapper<UserResponse> {
        override fun apply(response: HttpClientResponse): UserResponse {
            response.body().asInputStream().use {
                val message = String(it.readAllBytes(), StandardCharsets.UTF_8)
                return UserResponse(null, UserResponse.Error(response.code(), message))
            }
        }
    }

    data class UserResponse(val payload: Payload?, val error: Error?) {
        data class Error(val code: Int, val message: String)
        data class Payload(val message: String)
    }

    @ResponseCodeMapper(code = DEFAULT, mapper = ResponseErrorMapper::class)
    @ResponseCodeMapper(code = 200, mapper = ResponseSuccessMapper::class)
    @HttpRoute(method = HttpMethod.GET, path = "/mapping_by_code/{code}")
    fun get(@Path code: String): UserResponse
}

