package ru.tinkoff.kora.guide.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.validation.controller.UserController;
import ru.tinkoff.kora.guide.validation.dto.UserRequest;
import ru.tinkoff.kora.guide.validation.dto.UserResponse;
import ru.tinkoff.kora.guide.validation.service.UserService;
import ru.tinkoff.kora.http.common.HttpResponseEntity;
import ru.tinkoff.kora.http.server.common.HttpServerResponse;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;
import ru.tinkoff.kora.validation.common.ViolationException;

@KoraAppTest(Application.class)
class ValidationAppTest {

    @TestComponent
    private UserController userController;
    @TestComponent
    private UserService userService;

    @Test
    void guideComponentsAreWired() {
        assertNotNull(userController);
        assertNotNull(userService);
    }

    @Test
    void createUserReturnsCreatedUser() {
        HttpResponseEntity<UserResponse> response = userController.createUser(new UserRequest("John Doe", "john@example.com"));

        assertEquals(201, response.code());
        assertNotNull(response.body());
        assertEquals("John Doe", response.body().name());
    }

    @Test
    void getUserReturnsStoredUser() {
        var created = userController.createUser(new UserRequest("Jane Doe", "jane@example.com"));

        var user = userController.getUser(created.body().id());

        assertEquals("Jane Doe", user.name());
        assertEquals("jane@example.com", user.email());
    }

    @Test
    void getUsersReturnsPagedUsers() {
        userController.createUser(new UserRequest("Alice", "alice@example.com"));
        userController.createUser(new UserRequest("Bob", "bob@example.com"));

        List<?> users = userController.getUsers(0, 1, "name");

        assertEquals(1, users.size());
    }

    @Test
    void updateUserReturnsUpdatedUser() {
        var created = userController.createUser(new UserRequest("Chris", "chris@example.com"));

        var updated = userController.updateUser(created.body().id(), new UserRequest("Chris Updated", "updated@example.com"));

        assertEquals(200, updated.code());
        assertEquals("Chris Updated", updated.body().name());
        assertEquals("updated@example.com", updated.body().email());
    }

    @Test
    void deleteUserReturnsNoContent() {
        var created = userController.createUser(new UserRequest("Delete Me", "delete@example.com"));

        HttpServerResponse response = userController.deleteUser(created.body().id());

        assertEquals(204, response.code());
    }

    @Test
    void controllerRejectsInvalidEmail() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.createUser(new UserRequest("John Doe", "not-an-email")));

        assertTrue(exception.getMessage().contains("email"));
    }

    @Test
    void controllerRejectsBlankName() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.createUser(new UserRequest("", "john@example.com")));

        assertTrue(exception.getMessage().contains("name"));
    }

    @Test
    void controllerRejectsInvalidUserId() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.getUser("abc"));

        assertTrue(exception.getMessage().contains("userId"));
    }

    @Test
    void controllerRejectsInvalidPage() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.getUsers(-1, 10, "name"));

        assertTrue(exception.getMessage().contains("page"));
    }

    @Test
    void controllerRejectsInvalidSize() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.getUsers(0, 0, "name"));

        assertTrue(exception.getMessage().contains("size"));
    }

    @Test
    void controllerRejectsInvalidSort() {
        ViolationException exception = assertThrows(
            ViolationException.class,
            () -> userController.getUsers(0, 10, "nickname"));

        assertTrue(exception.getMessage().contains("sort"));
    }

    @Test
    void updateStillReturnsNotFoundForMissingUser() {
        HttpServerResponseException exception = assertThrows(
            HttpServerResponseException.class,
            () -> userController.updateUser("999", new UserRequest("Ghost", "ghost@example.com")));

        assertEquals(404, exception.code());
    }
}
