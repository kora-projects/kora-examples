package ru.tinkoff.kora.guide.httpserver

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.httpserver.controller.UserController
import ru.tinkoff.kora.guide.httpserver.dto.UserRequest
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class HttpServerAppTest {
    @TestComponent
    lateinit var userController: UserController

    @Test
    fun componentsAreWired() {
        assertNotNull(userController)
    }

    @Test
    fun createUserReturnsResponse() {
        val created = userController.createUser(UserRequest("John", "john@example.com"))
        assertEquals(201, created.code())
        assertNotNull(created.body()!!.id)
        assertEquals("John", created.body()!!.name)
        assertEquals("john@example.com", created.body()!!.email)
        assertNotNull(created.body()!!.createdAt)
    }
}
