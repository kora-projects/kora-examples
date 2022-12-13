package ru.tinkoff.kora.example.scheduling.quartz;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import ru.tinkoff.kora.scheduling.quartz.KoraQuartzJob;
import ru.tinkoff.kora.scheduling.quartz.KoraQuartzJobRegistrar;
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest;
import ru.tinkoff.kora.test.extension.junit5.TestComponent;

@KoraAppTest(value = Application.class, components = { KoraQuartzJob.class, KoraQuartzJobRegistrar.class })
class CronSchedulerTests {

    @TestComponent
    private CronScheduler scheduler;

    @Test
    void scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(4)).until(() -> scheduler.getState() > 2);
    }
}
