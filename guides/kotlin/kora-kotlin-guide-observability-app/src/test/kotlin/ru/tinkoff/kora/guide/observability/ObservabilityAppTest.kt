package ru.tinkoff.kora.guide.observability

import io.micrometer.core.instrument.MeterRegistry
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.guide.observability.dto.UserRequest
import ru.tinkoff.kora.guide.observability.health.ApplicationHealthProbe
import ru.tinkoff.kora.guide.observability.health.CustomReadinessProbe
import ru.tinkoff.kora.guide.observability.service.UserService
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent

@KoraAppTest(Application::class)
class ObservabilityAppTest {
    @TestComponent
    lateinit var userService: UserService

    @TestComponent
    lateinit var meterRegistry: MeterRegistry

    @TestComponent
    lateinit var livenessProbe: ApplicationHealthProbe

    @TestComponent
    lateinit var readinessProbe: CustomReadinessProbe

    @Test
    fun userCreationUpdatesCustomMetrics() {
        userService.createUser(UserRequest("Alice", "alice@example.com"))

        val counter = meterRegistry.find("user.creation.total").counter()
        val timer = meterRegistry.find("user.creation.duration").timer()

        assertNotNull(counter)
        assertNotNull(timer)
        assertEquals(1.0, counter!!.count())
        assertEquals(1L, timer!!.count())
    }

    @Test
    fun probesEventuallyReportHealthyState() {
        assertNull(livenessProbe.probe())

        repeat(10) {
            if (readinessProbe.probe() == null) {
                return
            }
            Thread.sleep(100L)
        }

        assertNull(readinessProbe.probe())
    }
}
