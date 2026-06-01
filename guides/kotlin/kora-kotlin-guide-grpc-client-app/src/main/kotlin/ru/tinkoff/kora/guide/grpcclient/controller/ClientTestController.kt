package ru.tinkoff.kora.guide.grpcclient.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcclient.dto.UserRequest
import ru.tinkoff.kora.guide.grpcclient.service.UserClientService
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientTestController(
    private val userClientService: UserClientService
) {

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-user-endpoints")
    @Json
    fun testAllUserEndpoints(): TestResults {
        return try {
            val created = userClientService.createUser(UserRequest("Client Demo User", "client-demo@example.com"))
            val fetched = userClientService.getUser(created.id)
            val users = userClientService.getUsers(0, 10, "name")
            val updated = userClientService.updateUser(
                created.id,
                UserRequest("Updated Client Demo User", "updated-client-demo@example.com")
            )
            userClientService.deleteUser(created.id)

            val userCreated = true
            val userFetched = created.id == fetched.id
            val usersListed = users.any { it.id == created.id }
            val userUpdated = updated.name == "Updated Client Demo User"
            val userDeleted = true
            val allTestsPassed = userCreated && userFetched && usersListed && userUpdated && userDeleted
            TestResults(userCreated, userFetched, usersListed, userUpdated, userDeleted, allTestsPassed, null)
        } catch (exception: Exception) {
            TestResults(false, false, false, false, false, false, exception.message)
        }
    }

    @Json
    data class TestResults(
        val userCreated: Boolean,
        val userFetched: Boolean,
        val usersListed: Boolean,
        val userUpdated: Boolean,
        val userDeleted: Boolean,
        val allTestsPassed: Boolean,
        val error: String?
    )
}
