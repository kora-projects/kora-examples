package ru.tinkoff.kora.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.repository.InMemoryUserRepository;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

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
