package io.koraframework.guide.observability;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import io.koraframework.guide.observability.dto.UserRequest;
import io.koraframework.guide.observability.health.ApplicationHealthProbe;
import io.koraframework.guide.observability.health.CustomReadinessProbe;
import io.koraframework.guide.observability.service.UserService;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

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
