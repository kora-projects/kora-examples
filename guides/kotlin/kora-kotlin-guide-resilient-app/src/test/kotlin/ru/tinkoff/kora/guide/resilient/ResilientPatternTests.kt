package ru.tinkoff.kora.guide.resilient

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.resilient.controller.UserController
import ru.tinkoff.kora.guide.resilient.dto.UserRequest
import ru.tinkoff.kora.guide.resilient.repository.InMemoryUserRepository
import ru.tinkoff.kora.guide.resilient.service.UserService
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class ResilientAppWiringTest {
    @TestComponent
    lateinit var userController: UserController

    @TestComponent
    lateinit var userService: UserService

    @Test
    fun guideComponentsAreWired() {
        assertNotNull(userController)
        assertNotNull(userService)
    }
}

@KoraAppTest(Application::class)
class FallbackPatternTest {
    @TestComponent
    lateinit var userService: UserService

    @Test
    fun createUserReturnsFallbackStubWhenPrimaryPathFails() {
        val result = userService.createUser(UserRequest("fallback-create", "fallback@example.com"))

        assertEquals("fallback-create", result.name)
        assertEquals("fallback@example.com", result.email)
        assertEquals("pending-file-write", result.id)
    }
}

@KoraAppTest(Application::class)
class RetryPatternTest {
    @TestComponent
    lateinit var userService: UserService

    @Test
    fun getUserEventuallySucceedsBecauseRetryIsApplied() {
        val created = userService.createUser(UserRequest("retry-user", "retry@example.com"))

        val result = userService.getUser(created.id)

        assertNotNull(result)
        assertEquals(created.id, result!!.id)
    }
}

@KoraAppTest(Application::class)
class TimeoutPatternTest {
    @TestComponent
    lateinit var userService: UserService

    @Test
    fun deleteUserFailsWhenOperationExceedsTimeout() {
        val created = userService.createUser(UserRequest("slow-delete", "delete@example.com"))

        assertThrows(RuntimeException::class.java) { userService.deleteUser(created.id) }
        assertNotNull(userService.getUser(created.id))
    }

    @Test
    fun deleteUserSucceedsWhenOperationFitsIntoTimeout() {
        val created = userService.createUser(UserRequest("delete-fast", "delete-fast@example.com"))

        userService.deleteUser(created.id)

        assertFalse(userService.getUser(created.id) != null)
    }
}

@KoraAppTest(Application::class)
class CircuitBreakerPatternTest {
    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var userRepository: InMemoryUserRepository

    @Test
    fun updateUserFailsFastAfterCircuitBreakerOpens() {
        val created = userService.createUser(UserRequest("breaker-source", "breaker@example.com"))
        val request = UserRequest("breaker-update", "updated-breaker@example.com")

        assertThrows(RuntimeException::class.java) { userService.updateUser(created.id, request) }
        assertThrows(RuntimeException::class.java) { userService.updateUser(created.id, request) }
        assertThrows(RuntimeException::class.java) { userService.updateUser(created.id, request) }

        assertEquals(2, userRepository.updateInvocations(created.id))
    }

    @Test
    fun updateUserNotFoundDoesNotTripCircuitBreakerBecause404IsIgnored() {
        val request = UserRequest("regular-update", "regular-update@example.com")

        assertEquals(
            404,
            assertThrows(HttpServerResponseException::class.java) {
                userService.updateUser(
                    "missing-user",
                    request
                )
            }.code()
        )
        assertEquals(
            404,
            assertThrows(HttpServerResponseException::class.java) {
                userService.updateUser(
                    "missing-user",
                    request
                )
            }.code()
        )
        assertEquals(
            404,
            assertThrows(HttpServerResponseException::class.java) {
                userService.updateUser(
                    "missing-user",
                    request
                )
            }.code()
        )

        assertEquals(3, userRepository.updateInvocations("missing-user"))
    }
}

@KoraAppTest(Application::class)
class CombinedPatternsTest {
    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var userRepository: InMemoryUserRepository

    @Test
    fun getUsersFailsAfterRetryTimeoutAndCircuitBreakerChain() {
        userService.createUser(UserRequest("slow-list", "slow-list@example.com"))
        userService.createUser(UserRequest("regular-user", "regular@example.com"))

        assertThrows(RuntimeException::class.java) { userService.getUsers(0, 10, "name") }
        assertTrue(userRepository.findAllInvocations() >= 2)
    }
}
