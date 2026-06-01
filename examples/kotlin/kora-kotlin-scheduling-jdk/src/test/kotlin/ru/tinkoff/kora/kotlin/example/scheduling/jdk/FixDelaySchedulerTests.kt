package ru.tinkoff.kora.kotlin.example.scheduling.jdk

import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import ru.tinkoff.kora.scheduling.jdk.FixedDelayJob
import ru.tinkoff.kora.test.extension.junit5.KoraAppTest
import ru.tinkoff.kora.test.extension.junit5.TestComponent
import java.time.Duration

@KoraAppTest(value = Application::class, components = [FixedDelayJob::class])
class FixDelaySchedulerTests {

    @TestComponent
    lateinit var scheduler: FixDelayScheduler

    @Test
    fun scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until { scheduler.state > 3 }
    }
}
