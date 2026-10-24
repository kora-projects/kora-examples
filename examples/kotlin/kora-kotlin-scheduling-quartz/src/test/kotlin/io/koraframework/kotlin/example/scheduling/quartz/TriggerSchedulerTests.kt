package io.koraframework.kotlin.example.scheduling.quartz

import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import io.koraframework.scheduling.quartz.KoraQuartzJob
import io.koraframework.scheduling.quartz.KoraQuartzJobRegistrar
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent
import java.time.Duration

@KoraAppTest(value = Application::class, components = [KoraQuartzJob::class, KoraQuartzJobRegistrar::class])
class TriggerSchedulerTests {

    @TestComponent
    lateinit var scheduler: TriggerScheduler

    @Test
    fun scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until { scheduler.state > 3 }
    }
}
