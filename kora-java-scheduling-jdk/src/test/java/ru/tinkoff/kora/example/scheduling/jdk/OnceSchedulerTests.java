package ru.tinkoff.kora.example.scheduling.jdk;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.scheduling.jdk.RunOnceJob;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(value = Application.class, components = RunOnceJob.class)
class OnceSchedulerTests {

    @TestComponent
    private OnceScheduler scheduler;

    @Test
    void scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> scheduler.getState() == 1);
        Assertions.assertThrows(ConditionTimeoutException.class,
                () -> Awaitility.await()
                        .atMost(Duration.ofSeconds(1))
                        .until(() -> scheduler.getState() != 1));
    }
}
