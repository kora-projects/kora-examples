package io.koraframework.guide.httpclient.client

import io.koraframework.guide.httpclient.dto.UserRequest
import io.koraframework.guide.httpclient.dto.UserResponse
import io.koraframework.http.client.common.annotation.HttpClient
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.*
import io.koraframework.json.common.annotation.Json

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
