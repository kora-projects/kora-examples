package io.koraframework.guide.httpclient.client

import io.koraframework.common.annotation.Component
import io.koraframework.guide.httpclient.dto.UserRequest
import io.koraframework.guide.httpclient.dto.UserResponse
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.client.common.response.HttpClientResponse
import io.koraframework.http.client.common.response.HttpClientResponseMapper
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.json.common.annotation.Json

@HttpClient("httpClient.userApi")
interface UserApiClient {

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(
        @Json request: UserRequest,
        @Header("X-Request-ID") requestId: String?,
        @Header("User-Agent") userAgent: String?,
        @Cookie("sessionId") sessionId: String?
    ): HttpResponseEntity<UserResponse>

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    fun getUser(@Path userId: String): UserResponse

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    fun getUsers(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("sort") sort: String?
    ): List<UserResponse>

    /**
     * Kora 2.0 ships response mappers for `String` and `ByteArray` only, so a body-less response
     * that still needs its status code has to say how `Void` is produced. The framework wraps it
     * into `HttpResponseEntity<Void>` through its own template factory, which is why the component
     * is declared but never referenced with `@Mapping`.
     */
    @Component
    class VoidResponseMapper : HttpClientResponseMapper<Void> {

        override fun apply(response: HttpClientResponse): Void? {
            response.body().use { body ->
                body.asInputStream().readAllBytes()
            }
            return null
        }
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpResponseEntity<Void>
}
