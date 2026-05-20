package ru.tinkoff.kora.kotlin.example.scheduling.quartz

import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.scheduling.quartz.KoraQuartzJob
import ru.tinkoff.kora.scheduling.quartz.KoraQuartzJobRegistrar
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
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
