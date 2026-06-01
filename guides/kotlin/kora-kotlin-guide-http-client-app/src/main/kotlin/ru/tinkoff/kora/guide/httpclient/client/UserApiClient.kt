package ru.tinkoff.kora.guide.httpclient.client

import ru.tinkoff.kora.guide.httpclient.dto.UserRequest
import ru.tinkoff.kora.guide.httpclient.dto.UserResponse
import ru.tinkoff.kora.http.client.common.annotation.HttpClient
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.HttpResponseEntity
import ru.tinkoff.kora.http.common.annotation.*
import ru.tinkoff.kora.json.common.annotation.Json

@HttpClient(configPath = "httpClient.userApi")
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

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpResponseEntity<Void>
}
