package ru.tinkoff.kora.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class FallbackPatternTest {

    @TestComponent
    private UserService userService;

    @Test
    void createUserReturnsFallbackStubWhenPrimaryPathFails() {
        var result = this.userService.createUser(new UserRequest("fallback-create", "fallback@example.com"));

        assertEquals("fallback-create", result.name());
        assertEquals("fallback@example.com", result.email());
        assertEquals("pending-file-write", result.id());
    }
}

