package io.koraframework.example.scheduling.quartz;

import java.time.Duration;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import io.koraframework.scheduling.quartz.KoraQuartzJob;
import io.koraframework.scheduling.quartz.KoraQuartzJobRegistrar;
import io.koraframework.test.extension.junit5.KoraAppTest;
import io.koraframework.test.extension.junit5.TestComponent;

@KoraAppTest(value = Application.class, components = { KoraQuartzJob.class, KoraQuartzJobRegistrar.class })
class TriggerSchedulerTests {

    @TestComponent
    private TriggerScheduler scheduler;

    @Test
    void scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until(() -> scheduler.getState() > 3);
    }
}
