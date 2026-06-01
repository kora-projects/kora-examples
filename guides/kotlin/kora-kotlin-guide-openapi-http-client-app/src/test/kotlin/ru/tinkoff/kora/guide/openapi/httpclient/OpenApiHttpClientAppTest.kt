package ru.tinkoff.kora.guide.openapi.httpclient

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.testcontainers.containers.output.Slf4jLogConsumer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApi
import ru.tinkoff.kora.guide.openapi.httpclient.user.api.UsersApiResponses
import ru.tinkoff.kora.guide.openapi.httpclient.user.model.UserRequestTO
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTestConfigModifier
import ru.tinkoff.kora.test.extension.junit5.KoraConfigModification
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.util.*

@Testcontainers
@KoraAppTest(Application::class)
class OpenApiHttpClientAppTest : KoraAppTestConfigModifier {
    @TestComponent
    lateinit var usersApi: UsersApi

    override fun config(): KoraConfigModification =
        KoraConfigModification.ofResourceFile("application.conf")
            .withSystemProperty("PUBLIC_API_URL", APP.getURI().toString())

    @Test
    fun createUserReturnsCreatedUserFromContainerizedOpenApiHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val response = usersApi.createUser(UserRequestTO("Client User $unique", "client-$unique@example.com"))
        val create201 =
            assertInstanceOf(UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java, response)

        assertNotNull(create201.content)
        assertEquals("Client User $unique", create201.content.name)
    }

    @Test
    fun getUserReturnsUserResponseFromContainerizedOpenApiHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            usersApi.createUser(UserRequestTO("Lookup User $unique", "lookup-$unique@example.com"))
        ).content

        val response = usersApi.getUser(created.id)
        val getUser200 = assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse::class.java, response)

        assertEquals(created.id, getUser200.content.id)
        assertEquals("Lookup User $unique", getUser200.content.name)
    }

    @Test
    fun getMissingUserReturnsNotFoundResponseFromContainerizedOpenApiHttpServerApp() {
        val response = usersApi.getUser("999999")
        assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse::class.java, response)
    }

    @Test
    fun getUsersPassesPagingAndSortingAgainstContainerizedOpenApiHttpServerApp() {
        val suffix = UUID.randomUUID().toString().substring(0, 6)
        val userA = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            usersApi.createUser(UserRequestTO("Alpha $suffix", "a-$suffix@example.com"))
        ).content
        val userB = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            usersApi.createUser(UserRequestTO("Bravo $suffix", "b-$suffix@example.com"))
        ).content
        val userC = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            usersApi.createUser(UserRequestTO("Charlie $suffix", "c-$suffix@example.com"))
        ).content

        val usersResponse = usersApi.getUsers(0, 100, "name")
        val users = assertInstanceOf(UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse::class.java, usersResponse).content
        val ids = listOf(userA.id, userB.id, userC.id)
        val filtered = users.filter { it.id in ids }

        assertEquals(3, filtered.size)
        assertEquals(listOf("Alpha $suffix", "Bravo $suffix", "Charlie $suffix"), filtered.map { it.name })
    }

    @Test
    fun deleteUserReturnsNoContentResponseFromContainerizedOpenApiHttpServerApp() {
        val unique = UUID.randomUUID().toString().substring(0, 8)
        val created = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            usersApi.createUser(UserRequestTO("Delete Me $unique", "delete-$unique@example.com"))
        ).content

        val deleteResponse = usersApi.deleteUser(created.id)
        assertInstanceOf(UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse::class.java, deleteResponse)
    }

    companion object {
        @Container
        @JvmStatic
        val APP: AppContainer = AppContainer()
            .withLogConsumer(Slf4jLogConsumer(LoggerFactory.getLogger(AppContainer::class.java)))
    }
}
