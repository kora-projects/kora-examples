package io.koraframework.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import io.koraframework.guide.resilient.dto.UserRequest;
import io.koraframework.guide.resilient.repository.InMemoryUserRepository;
import io.koraframework.guide.resilient.service.UserService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class CombinedPatternsTest {

    @TestComponent
    private UserService userService;
    @TestComponent
    private InMemoryUserRepository userRepository;

    @Test
    void getUsersFailsAfterRetryTimeoutAndCircuitBreakerChain() {
        this.userService.createUser(new UserRequest("slow-list", "slow-list@example.com"));
        this.userService.createUser(new UserRequest("regular-user", "regular@example.com"));

        assertThrows(RuntimeException.class, () -> this.userService.getUsers(0, 10, "name"));
        assertTrue(this.userRepository.findAllInvocations() >= 2);
    }
}
