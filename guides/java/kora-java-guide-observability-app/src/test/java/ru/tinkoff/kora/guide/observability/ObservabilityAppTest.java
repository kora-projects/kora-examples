package ru.tinkoff.kora.guide.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.guide.observability.dto.UserRequest;
import ru.tinkoff.kora.guide.observability.health.ApplicationHealthProbe;
import ru.tinkoff.kora.guide.observability.health.CustomReadinessProbe;
import ru.tinkoff.kora.guide.observability.service.UserService;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(Application.class)
class ObservabilityAppTest {

    @TestComponent
    private UserService userService;
    @TestComponent
    private MeterRegistry meterRegistry;
    @TestComponent
    private ApplicationHealthProbe livenessProbe;
    @TestComponent
    private CustomReadinessProbe readinessProbe;

    @Test
    void userCreationUpdatesCustomMetrics() {
        this.userService.createUser(new UserRequest("Alice", "alice@example.com"));

        var counter = this.meterRegistry.find("user.creation.total").counter();
        var timer = this.meterRegistry.find("user.creation.duration").timer();

        assertNotNull(counter);
        assertNotNull(timer);
        assertEquals(1.0d, counter.count());
        assertEquals(1L, timer.count());
    }

    @Test
    void probesEventuallyReportHealthyState() throws Exception {
        assertNull(this.livenessProbe.probe());

        for (int i = 0; i < 10; i++) {
            if (this.readinessProbe.probe() == null) {
                return;
            }
            Thread.sleep(100L);
        }

        assertNull(this.readinessProbe.probe());
    }
}
