package ru.tinkoff.kora.guide.httpclient

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.guide.httpclient.client.UserApiClient
import ru.tinkoff.kora.guide.httpclient.dto.UserRequest
import ru.tinkoff.kora.http.client.common.HttpClientResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.util.*

@Testcontainers
@KoraAppTest(Application::class)
class HttpClientAppTest : KoraAppTestConfigModifier {
    @TestComponent
    lateinit var userApiClient: UserApiClient

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofResourceFile("application.conf")
            .withSystemProperty("USER_API_URL", APP.getURI().toString())

    @Test
    fun createUserReturnsCreatedUserFromContainerizedHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val response = userApiClient.createUser(
            UserRequest("Client User $unique", "client-$unique@example.com"),
            "request-1",
            "test-agent",
            "session-1"
        )

        assertEquals(201, response.code())
        assertNotNull(response.body())
        assertEquals("Client User $unique", response.body()!!.name)
    }

    @Test
    fun getUserReturnsUserResponseFromContainerizedHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userApiClient.createUser(
            UserRequest("Lookup User $unique", "lookup-$unique@example.com"),
            "request-2",
            "test-agent",
            "session-2"
        ).body()!!

        val user = userApiClient.getUser(created.id)

        assertEquals(created.id, user.id)
        assertEquals("Lookup User $unique", user.name)
    }

    @Test
    fun getMissingUserThrowsAgainstContainerizedHttpServerApp() {
        assertThrows(HttpClientResponseException::class.java) { userApiClient.getUser("999999") }
    }

    @Test
    fun getUsersPassesPagingAndSortingAgainstContainerizedHttpServerApp() {
        val suffix = UUID.randomUUID().toString().substring(0, 6)
        val userA = userApiClient.createUser(UserRequest("Alpha $suffix", "a-$suffix@example.com"), "req-a", "agent", "s1").body()!!
        val userB = userApiClient.createUser(UserRequest("Bravo $suffix", "b-$suffix@example.com"), "req-b", "agent", "s2").body()!!
        val userC = userApiClient.createUser(UserRequest("Charlie $suffix", "c-$suffix@example.com"), "req-c", "agent", "s3").body()!!

        val users = userApiClient.getUsers(0, 100, "name")
        val ids = listOf(userA.id, userB.id, userC.id)
        val filtered = users.filter { it.id in ids }

        assertEquals(3, filtered.size)
        assertEquals(listOf("Alpha $suffix", "Bravo $suffix", "Charlie $suffix"), filtered.map { it.name })
    }

    @Test
    fun deleteUserReturnsNoContentFromContainerizedHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = userApiClient.createUser(
            UserRequest("Delete Me $unique", "delete-$unique@example.com"),
            "request-3",
            "test-agent",
            "session-3"
        ).body()!!

        val response = userApiClient.deleteUser(created.id)

        assertEquals(204, response.code())
    }

    companion object {
        @Container
        @JvmStatic
        val APP: AppContainer = AppContainer()
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }
}
