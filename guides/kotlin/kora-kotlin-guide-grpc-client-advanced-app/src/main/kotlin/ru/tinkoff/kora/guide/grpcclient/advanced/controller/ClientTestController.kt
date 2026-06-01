package ru.tinkoff.kora.guide.grpcclient.advanced.controller

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserRequest
import ru.tinkoff.kora.guide.grpcclient.advanced.dto.UserUpdateRequest
import ru.tinkoff.kora.guide.grpcclient.advanced.service.UserStreamingClientService
import ru.tinkoff.kora.http.common.HttpMethod
import ru.tinkoff.kora.http.common.annotation.HttpRoute
import ru.tinkoff.kora.http.server.common.annotation.HttpController
import ru.tinkoff.kora.json.common.annotation.Json

@Component
@HttpController
class ClientTestController(
    private val userStreamingClientService: UserStreamingClientService
) {

    @HttpRoute(method = HttpMethod.POST, path = "/client/test-all-streaming-endpoints")
    @Json
    fun testAllStreamingEndpoints(): TestResults {
        return try {
            val created = userStreamingClientService.createUsers(
                listOf(
                    UserRequest("Alice Streaming", "alice-streaming@example.com"),
                    UserRequest("Bob Streaming", "bob-streaming@example.com")
                )
            )
            val usersCreated = created.createdCount == 2

            val streamed = userStreamingClientService.getAllUsers()
            val usersStreamed = created.userIds.all { userId -> streamed.any { user -> user.id == userId } }

            val updated = userStreamingClientService.updateUsers(
                listOf(
                    UserUpdateRequest(created.userIds[0], "Updated Alice Streaming", "updated-alice@example.com"),
                    UserUpdateRequest(created.userIds[1], "Updated Bob Streaming", "updated-bob@example.com")
                )
            )
            val usersUpdated = updated.any { it.name == "Updated Alice Streaming" } &&
                    updated.any { it.name == "Updated Bob Streaming" }

            val allTestsPassed = usersCreated && usersStreamed && usersUpdated
            TestResults(usersCreated, usersStreamed, usersUpdated, allTestsPassed, null)
        } catch (exception: Exception) {
            TestResults(false, false, false, false, exception.message)
        }
    }

    @Json
    data class TestResults(
        val usersCreated: Boolean,
        val usersStreamed: Boolean,
        val usersUpdated: Boolean,
        val allTestsPassed: Boolean,
        val error: String?
    )
}
