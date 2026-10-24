package io.koraframework.guide.httpclient.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.httpclient.client.UserApiClient
import io.koraframework.guide.httpclient.dto.UserRequest
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

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
