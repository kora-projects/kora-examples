package ru.tinkoff.kora.example.scheduling.jdk;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.scheduling.jdk.FixedDelayJob;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(value = Application.class, components = FixedDelayJob.class)
class FixDelaySchedulerTests {

    @TestComponent
    private FixDelayScheduler scheduler;

    @Test
    void scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> scheduler.getState() > 3);
    }
}
