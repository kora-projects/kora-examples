package ru.tinkoff.kora.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class TimeoutPatternTest {

    @TestComponent
    private UserService userService;

    @Test
    void deleteUserFailsWhenOperationExceedsTimeout() {
        var created = this.userService.createUser(new UserRequest("slow-delete", "delete@example.com"));

        assertThrows(RuntimeException.class, () -> this.userService.deleteUser(created.id()));
        assertTrue(this.userService.getUser(created.id()).isPresent());
    }

    @Test
    void deleteUserSucceedsWhenOperationFitsIntoTimeout() {
        var created = this.userService.createUser(new UserRequest("delete-fast", "delete-fast@example.com"));

        this.userService.deleteUser(created.id());

        assertFalse(this.userService.getUser(created.id()).isPresent());
    }
}
