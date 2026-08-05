package io.koraframework.guide.grpcclient.advanced.controller

import io.koraframework.common.annotation.Component
import io.koraframework.guide.grpcclient.advanced.dto.UserRequest
import io.koraframework.guide.grpcclient.advanced.dto.UserUpdateRequest
import io.koraframework.guide.grpcclient.advanced.service.UserStreamingClientService
import io.koraframework.http.common.HttpMethod
import io.koraframework.http.common.annotation.HttpRoute
import io.koraframework.http.server.common.annotation.HttpController
import io.koraframework.json.common.annotation.Json

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
