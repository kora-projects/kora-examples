package ru.tinkoff.kora.guide.testingjunit

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import ru.tinkoff.kora.guide.httpserver.Application
import ru.tinkoff.kora.guide.httpserver.dto.UserRequest
import ru.tinkoff.kora.guide.httpserver.dto.UserResponse
import ru.tinkoff.kora.guide.httpserver.repository.UserRepository
import ru.tinkoff.kora.guide.httpserver.service.UserService
import ru.tinkoff.kora.http.server.common.HttpServerResponseException
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.LocalDateTime

@KoraAppTest(Application::class)
class UserServiceComponentTest {

    @Mock
    @TestComponent
    lateinit var userRepository: UserRepository

    @TestComponent
    lateinit var userService: UserService

    @Test
    fun createUserShouldCreateAndReturnUser() {
        val request = UserRequest("John", "john@example.com")
        `when`(userRepository.save("John", "john@example.com")).thenReturn("1")

        val result = userService.createUser(request)

        assertNotNull(result)
        assertEquals("1", result.id)
        assertEquals("John", result.name)
        assertEquals("john@example.com", result.email)
        verify(userRepository).save("John", "john@example.com")
    }

    @Test
    fun getUserShouldReturnUserWhenExists() {
        val expected = UserResponse("1", "John", "john@example.com", LocalDateTime.now())
        `when`(userRepository.findById("1")).thenReturn(expected)

        val result = userService.getUser("1")

        assertEquals(expected, result)
        verify(userRepository).findById("1")
    }

    @Test
    fun getUsersShouldReturnPagedUsers() {
        val users = listOf(
            UserResponse("2", "Jane", "jane@example.com", LocalDateTime.now()),
            UserResponse("1", "John", "john@example.com", LocalDateTime.now())
        )
        `when`(userRepository.findAll()).thenReturn(users)

        val result = userService.getUsers(0, 10, "name")

        assertEquals(2, result.size)
        assertEquals("Jane", result[0].name)
        assertEquals("John", result[1].name)
        verify(userRepository).findAll()
    }

    @Test
    fun updateUserShouldUpdateAndReturnUserWhenExists() {
        val request = UserRequest("John Updated", "john.updated@example.com")
        `when`(userRepository.update("1", request.name, request.email)).thenReturn(true)

        val result = userService.updateUser("1", request)

        assertEquals("1", result.id)
        assertEquals("John Updated", result.name)
        assertEquals("john.updated@example.com", result.email)
        verify(userRepository).update("1", request.name, request.email)
    }

    @Test
    fun deleteUserShouldCallRepositoryWhenUserExists() {
        `when`(userRepository.deleteById("1")).thenReturn(true)

        userService.deleteUser("1")

        verify(userRepository).deleteById("1")
    }

    @Test
    fun deleteUserShouldThrow404WhenUserMissing() {
        `when`(userRepository.deleteById("missing")).thenReturn(false)

        val exception = assertThrows(HttpServerResponseException::class.java) { userService.deleteUser("missing") }

        assertEquals(404, exception.code())
        verify(userRepository).deleteById("missing")
    }
}
