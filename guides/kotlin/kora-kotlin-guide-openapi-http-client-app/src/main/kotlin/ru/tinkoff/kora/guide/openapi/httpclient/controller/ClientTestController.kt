package ru.tinkoff.kora.guide.openapi.httpclient.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApi
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApiResponses
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserRequestTO
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientTestController(
    private val usersApi: UsersApi
) {
    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    fun testAllUserEndpoints(): TestResults {
        return try {
            val created = usersApi.createUser(UserRequestTO("Client Demo User", "client-demo@example.com"))
            val userCreated = created is UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse
            val createdUser =
                if (created is UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse) created.content else null

            val getUserResponse = createdUser?.let { usersApi.getUser(it.id) }
            val userFetched =
                getUserResponse is UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse &&
                        createdUser.id == getUserResponse.content.id

            val getUsersResponse = usersApi.getUsers(0, 10, "name")
            val users =
                if (getUsersResponse is UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse) {
                    getUsersResponse.content
                } else {
                    emptyList()
                }
            val usersListed = createdUser != null && users.any { it.id == createdUser.id }

            val deleteResult = createdUser?.let { usersApi.deleteUser(it.id) }
            val userDeleted = deleteResult is UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse

            val allTestsPassed = userCreated && userFetched && usersListed && userDeleted
            TestResults(userCreated, userFetched, usersListed, userDeleted, allTestsPassed, null)
        } catch (e: Exception) {
            TestResults(false, false, false, false, false, e.message)
        }
    }

    @Json
    data class TestResults(
        val userCreated: Boolean,
        val userFetched: Boolean,
        val usersListed: Boolean,
        val userDeleted: Boolean,
        val allTestsPassed: Boolean,
        val error: String?
    )
}
