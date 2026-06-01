package ru.tinkoff.kora.guide.openapi.httpserver

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiDelegate
import ru.tinkoff.kora.guide.openapi.httpserver.user.api.UsersApiResponses
import ru.tinkoff.kora.guide.openapi.httpserver.user.model.UserRequestTO
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class OpenApiHttpServerAppTest {
    @TestComponent
    lateinit var usersApiDelegate: UsersApiDelegate

    @Test
    fun crudFlowWorksThroughDelegate() {
        val createResponse = usersApiDelegate.createUser(UserRequestTO("John Doe", "john@example.com"))
        val create201 = assertInstanceOf(
            UsersApiResponses.CreateUserApiResponse.CreateUser201ApiResponse::class.java,
            createResponse
        )
        assertNotNull(create201.content)
        assertEquals("John Doe", create201.content.name)

        val getUserResponse = usersApiDelegate.getUser(create201.content.id)
        val getUser200 =
            assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser200ApiResponse::class.java, getUserResponse)
        assertEquals("john@example.com", getUser200.content.email)

        val getUsersResponse = usersApiDelegate.getUsers(0, 10, "name")
        val getUsers200 =
            assertInstanceOf(UsersApiResponses.GetUsersApiResponse.GetUsers200ApiResponse::class.java, getUsersResponse)
        assertEquals(1, getUsers200.content.size)

        val updateResponse =
            usersApiDelegate.updateUser(create201.content.id, UserRequestTO("John Updated", "john.updated@example.com"))
        val update200 = assertInstanceOf(
            UsersApiResponses.UpdateUserApiResponse.UpdateUser200ApiResponse::class.java,
            updateResponse
        )
        assertEquals("John Updated", update200.content.name)
        assertNotNull(update200.xUpdatedAt)

        val deleteResponse = usersApiDelegate.deleteUser(create201.content.id)
        assertInstanceOf(UsersApiResponses.DeleteUserApiResponse.DeleteUser204ApiResponse::class.java, deleteResponse)

        val getAfterDeleteResponse = usersApiDelegate.getUser(create201.content.id)
        assertInstanceOf(UsersApiResponses.GetUserApiResponse.GetUser404ApiResponse::class.java, getAfterDeleteResponse)
    }
}
