package ru.tinkoff.kora.guide.resilient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.resilient.dto.UserRequest;
import ru.tinkoff.kora.guide.resilient.repository.InMemoryUserRepository;
import ru.tinkoff.kora.guide.resilient.service.UserService;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class CircuitBreakerPatternTest {

    @TestComponent
    private UserService userService;
    @TestComponent
    private InMemoryUserRepository userRepository;

    @Test
    void updateUserFailsFastAfterCircuitBreakerOpens() {
        var created = this.userService.createUser(new UserRequest("breaker-source", "breaker@example.com"));
        var request = new UserRequest("breaker-update", "updated-breaker@example.com");

        assertThrows(RuntimeException.class, () -> this.userService.updateUser(created.id(), request));
        assertThrows(RuntimeException.class, () -> this.userService.updateUser(created.id(), request));
        assertThrows(RuntimeException.class, () -> this.userService.updateUser(created.id(), request));

        assertEquals(2, this.userRepository.updateInvocations(created.id()));
    }

    @Test
    void updateUserNotFoundDoesNotTripCircuitBreakerBecause404IsIgnored() {
        var request = new UserRequest("regular-update", "regular-update@example.com");

        assertEquals(404, assertThrows(HttpServerResponseException.class,
                () -> this.userService.updateUser("missing-user", request)).code());
        assertEquals(404, assertThrows(HttpServerResponseException.class,
                () -> this.userService.updateUser("missing-user", request)).code());
        assertEquals(404, assertThrows(HttpServerResponseException.class,
                () -> this.userService.updateUser("missing-user", request)).code());

        assertEquals(3, this.userRepository.updateInvocations("missing-user"));
    }
}
