package ru.tinkoff.kora.guide.validation

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.validation.controller.UserController
import ru.tinkoff.kora.guide.validation.dto.UserRequest
import ru.tinkoff.kora.guide.validation.service.UserService
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import ru.tinkoff.kora.validation.common.ViolationException

@KoraAppTest(Application::class)
class ValidationAppTest {
    @TestComponent
    lateinit var userController: UserController

    @TestComponent
    lateinit var userService: UserService

    @Test
    fun guideComponentsAreWired() {
        assertNotNull(userController); assertNotNull(userService)
    }

    @Test
    fun createUserReturnsCreatedUser() {
        val response = userController.createUser(UserRequest("John Doe", "john@example.com"))
        assertEquals(201, response.code())
        assertEquals("John Doe", response.body()!!.name)
    }

    @Test
    fun getUserReturnsStoredUser() {
        val created = userController.createUser(UserRequest("Jane Doe", "jane@example.com"))

        val user = userController.getUser(created.body()!!.id)

        assertEquals("Jane Doe", user.name)
        assertEquals("jane@example.com", user.email)
    }

    @Test
    fun getUsersReturnsPagedUsers() {
        userController.createUser(UserRequest("Alice", "alice@example.com"))
        userController.createUser(UserRequest("Bob", "bob@example.com"))

        val users = userController.getUsers(0, 1, "name")

        assertEquals(1, users.size)
    }

    @Test
    fun updateUserReturnsUpdatedUser() {
        val created = userController.createUser(UserRequest("Chris", "chris@example.com"))

        val updated = userController.updateUser(created.body()!!.id, UserRequest("Chris Updated", "updated@example.com"))

        assertEquals(200, updated.code())
        assertEquals("Chris Updated", updated.body()!!.name)
        assertEquals("updated@example.com", updated.body()!!.email)
    }

    @Test
    fun deleteUserReturnsNoContent() {
        val created = userController.createUser(UserRequest("Delete Me", "delete@example.com"))

        val response = userController.deleteUser(created.body()!!.id)

        assertEquals(204, response.code())
    }

    @Test
    fun controllerRejectsInvalidEmail() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.createUser(
                UserRequest(
                    "John Doe",
                    "not-an-email"
                )
            )
        }
        assertTrue(exception.message!!.contains("email"))
    }

    @Test
    fun controllerRejectsBlankName() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.createUser(
                UserRequest(
                    "",
                    "john@example.com"
                )
            )
        }
        assertTrue(exception.message!!.contains("name"))
    }

    @Test
    fun controllerRejectsInvalidUserId() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.getUser("abc")
        }

        assertTrue(exception.message!!.contains("userId"))
    }

    @Test
    fun controllerRejectsInvalidPage() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.getUsers(-1, 10, "name")
        }

        assertTrue(exception.message!!.contains("page"))
    }

    @Test
    fun controllerRejectsInvalidSize() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.getUsers(0, 0, "name")
        }

        assertTrue(exception.message!!.contains("size"))
    }

    @Test
    fun controllerRejectsInvalidSort() {
        val exception = assertThrows(ViolationException::class.java) {
            userController.getUsers(0, 10, "nickname")
        }

        assertTrue(exception.message!!.contains("sort"))
    }

    @Test
    fun updateStillReturnsNotFoundForMissingUser() {
        val exception = assertThrows(HttpServerResponseException::class.java) {
            userController.updateUser(
                "999",
                UserRequest("Ghost", "ghost@example.com")
            )
        }
        assertEquals(404, exception.code())
    }
}
