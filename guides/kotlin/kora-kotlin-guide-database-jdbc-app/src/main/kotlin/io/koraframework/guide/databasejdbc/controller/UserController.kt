package io.koraframework.guide.databasejdbc.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.databasejdbc.dto.UserRequest
import io.koraframework.guide.databasejdbc.dto.UserResponse
import io.koraframework.guide.databasejdbc.service.UserService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.HttpResponseEntity
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.common.annotation.Path
import io.koraframework.http.common.annotation.Query
import io.koraframework.http.common.body.HttpBody
import io.koraframework.http.common.header.HttpHeaders
import io.koraframework.http.server.common.response.HttpServerResponse
import io.koraframework.http.server.common.response.HttpServerResponseException
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

@Component
@HttpController
class UserController(
    private val userService: UserService
) {

    @HttpRoute(method = HttpMethod.GET, path = "/users/{userId}")
    @Json
    fun getUser(@Path userId: String): UserResponse {
        return userService.getUser(userId)
            ?: throw HttpServerResponseException.of(404, "User not found")
    }

    @HttpRoute(method = HttpMethod.GET, path = "/users")
    @Json
    fun getUsers(
        @Query("page") page: Int?,
        @Query("size") size: Int?,
        @Query("sort") sort: String?
    ): List<UserResponse> {
        return userService.getUsers(page ?: 0, size ?: 10, sort ?: "name")
    }

    @HttpRoute(method = HttpMethod.POST, path = "/users")
    @Json
    fun createUser(@Json request: UserRequest): HttpResponseEntity<UserResponse> {
        val user = userService.createUser(request)
        return HttpResponseEntity.of(201, HttpHeaders.of(), user)
    }

    @HttpRoute(method = HttpMethod.PUT, path = "/users/{userId}")
    @Json
    fun updateUser(@Path userId: String, @Json request: UserRequest): UserResponse {
        return userService.updateUser(userId, request)
    }

    @HttpRoute(method = HttpMethod.DELETE, path = "/users/{userId}")
    fun deleteUser(@Path userId: String): HttpServerResponse {
        userService.deleteUser(userId)
        return HttpServerResponse.of(204, HttpBody.empty())
    }
}
