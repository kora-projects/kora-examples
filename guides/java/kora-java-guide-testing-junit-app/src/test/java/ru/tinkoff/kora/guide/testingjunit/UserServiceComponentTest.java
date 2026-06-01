package ru.tinkoff.kora.guide.testingjunit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import ru.tinkoff.kora.guide.httpserver.Application;
import ru.tinkoff.kora.guide.httpserver.dto.UserRequest;
import ru.tinkoff.kora.guide.httpserver.dto.UserResponse;
import ru.tinkoff.kora.guide.httpserver.repository.UserRepository;
import ru.tinkoff.kora.guide.httpserver.service.UserService;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class UserServiceComponentTest {

    @Mock
    @TestComponent
    private UserRepository userRepository;

    @TestComponent
    private UserService userService;

    @Test
    void createUser_ShouldCreateAndReturnUser() {
        var request = new UserRequest("John", "john@example.com");

        when(userRepository.save("John", "john@example.com")).thenReturn("1");

        var result = userService.createUser(request);

        assertNotNull(result);
        assertEquals("1", result.id());
        assertEquals("John", result.name());
        assertEquals("john@example.com", result.email());
        verify(userRepository).save("John", "john@example.com");
    }

    @Test
    void getUser_ShouldReturnUserWhenExists() {
        var expected = new UserResponse("1", "John", "john@example.com", LocalDateTime.now());
        when(userRepository.findById("1")).thenReturn(Optional.of(expected));

        var result = userService.getUser("1");

        assertTrue(result.isPresent());
        assertEquals(expected, result.get());
        verify(userRepository).findById("1");
    }

    @Test
    void getUsers_ShouldReturnPagedUsers() {
        var users = List.of(
                new UserResponse("2", "Jane", "jane@example.com", LocalDateTime.now()),
                new UserResponse("1", "John", "john@example.com", LocalDateTime.now()));
        when(userRepository.findAll()).thenReturn(users);

        var result = userService.getUsers(0, 10, "name");

        assertEquals(2, result.size());
        assertEquals("Jane", result.get(0).name());
        assertEquals("John", result.get(1).name());
        verify(userRepository).findAll();
    }

    @Test
    void updateUser_ShouldUpdateAndReturnUserWhenExists() {
        var request = new UserRequest("John Updated", "john.updated@example.com");
        when(userRepository.update("1", request.name(), request.email())).thenReturn(true);

        var result = userService.updateUser("1", request);

        assertEquals("1", result.id());
        assertEquals("John Updated", result.name());
        assertEquals("john.updated@example.com", result.email());
        verify(userRepository).update("1", request.name(), request.email());
    }

    @Test
    void deleteUser_ShouldCallRepositoryWhenUserExists() {
        when(userRepository.deleteById("1")).thenReturn(true);

        userService.deleteUser("1");

        verify(userRepository).deleteById("1");
    }

    @Test
    void deleteUser_ShouldThrow404WhenUserMissing() {
        when(userRepository.deleteById("missing")).thenReturn(false);

        var exception = assertThrows(HttpServerResponseException.class, () -> userService.deleteUser("missing"));

        assertEquals(404, exception.code());
        verify(userRepository).deleteById("missing");
    }
}
