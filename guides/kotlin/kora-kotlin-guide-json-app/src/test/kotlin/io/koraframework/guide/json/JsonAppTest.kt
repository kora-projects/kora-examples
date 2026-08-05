package io.koraframework.guide.json

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import io.koraframework.guide.json.dto.UserRequest
import io.koraframework.guide.json.dto.UserResult
import io.koraframework.guide.json.service.UserService
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class JsonAppTest {
    @TestComponent
    lateinit var userService: UserService

    @Test
    fun componentsAreWired() {
        assertNotNull(userService)
    }

    @Test
    fun sealedJsonResponseFlow() {
        val created = userService.createUser(UserRequest("Alice", "alice@example.com"))
        val result = userService.getUser(created.id)

        val success = assertInstanceOf(UserResult.UserSuccess::class.java, result)
        assertEquals(UserResult.Status.OK, success.status)
        assertEquals("Alice", success.user.name)
    }
}
