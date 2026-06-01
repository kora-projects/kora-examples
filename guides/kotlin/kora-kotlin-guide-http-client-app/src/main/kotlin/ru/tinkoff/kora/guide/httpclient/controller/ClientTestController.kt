package ru.tinkoff.kora.guide.httpclient.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.httpclient.client.UserApiClient
import ru.tinkoff.kora.guide.httpclient.dto.UserRequest
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientTestController(
    private val userApiClient: UserApiClient
) {
    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    fun testAllUserEndpoints(): TestResults {
        return try {
            val created = userApiClient.createUser(
                UserRequest("Client Demo User", "client-demo@example.com"),
                "client-test-request",
                "guide-http-client-app",
                "client-test-session"
            )

            val userCreated = created.code() == 201 && created.body() != null
            val createdUser = created.body()
            val fetched = createdUser?.let { userApiClient.getUser(it.id) }
            val userFetched = fetched != null && createdUser != null && fetched.id == createdUser.id
            val users = userApiClient.getUsers(0, 10, "name")
            val usersListed = createdUser != null && users.any { it.id == createdUser.id }
            val deleteResult = createdUser?.let { userApiClient.deleteUser(it.id) }
            val userDeleted = deleteResult != null && deleteResult.code() == 204

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
