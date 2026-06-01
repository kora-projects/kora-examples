package ru.tinkoff.kora.guide.dependencyinjection

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class NotificationServiceTest {
    @TestComponent
    lateinit var notificationService: NotificationService

    @Test
    fun broadcastUsesAllTaggedAndUntaggedNotifiers() {
        val result = notificationService.broadcast("hello")
        assertEquals(2, result.size)
        assertTrue(result.contains("EMAIL: [app] hello"))
        assertTrue(result.contains("SMS: [app] hello"))
    }

    @Test
    fun emailOnlyUsesTaggedComponent() {
        assertEquals("EMAIL: [app] hello", notificationService.notifyEmailOnly("hello"))
    }

    @Test
    fun optionalAuditSinkIsAbsent() {
        assertFalse(notificationService.isAuditEnabled())
    }
}
