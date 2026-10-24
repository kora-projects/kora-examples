package io.koraframework.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import io.koraframework.guide.resilient.dto.UserRequest;
import io.koraframework.guide.resilient.service.UserService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class RetryPatternTest {

    @TestComponent
    private UserService userService;

    @Test
    void getUserEventuallySucceedsBecauseRetryIsApplied() {
        var created = this.userService.createUser(new UserRequest("retry-user", "retry@example.com"));

        var result = this.userService.getUser(created.id());

        assertTrue(result.isPresent());
        assertEquals(created.id(), result.get().id());
    }
}
