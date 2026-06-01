package ru.tinkoff.kora.guide.dependencyinjection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class NotificationServiceTest {

    @TestComponent
    private NotificationService notificationService;

    @Test
    void broadcast_ShouldUseAllNotifiers() {
        List<String> result = notificationService.broadcast("Hello");

        assertEquals(2, result.size());
        assertTrue(result.contains("EMAIL: [app] Hello"));
        assertTrue(result.contains("SMS: [app] Hello"));
    }

    @Test
    void notifyEmailOnly_ShouldResolveTaggedNotifier() {
        String result = notificationService.notifyEmailOnly("Ping");

        assertEquals("EMAIL: [app] Ping", result);
    }

    @Test
    void optionalAuditDependency_ShouldBeHandledGracefully() {
        assertFalse(notificationService.isAuditEnabled());
    }
}