package io.koraframework.kotlin.example.http.client

import io.koraframework.common.annotation.Component
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.annotation.ResponseCodeMapper
import io.koraframework.http.client.common.annotation.ResponseCodeMapper.DEFAULT
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.client.common.response.HttpClientResponseMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import java.nio.charset.StandardCharsets

@HttpClient("httpClient.default")
interface MapperResponseHttpClient {

    @Component
    class ResponseSuccessMapper : HttpClientResponseMapper<UserResponse> {
        override fun apply(response: HttpClientResponse): UserResponse {
            response.body().asInputStream().use {
                val message = String(it.readAllBytes(), StandardCharsets.UTF_8)
                return UserResponse(UserResponse.Payload(message), null)
            }
        }
    }

    @Component
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
