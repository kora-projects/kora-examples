package io.koraframework.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import io.koraframework.guide.resilient.dto.UserRequest;
import io.koraframework.guide.resilient.service.UserService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

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

