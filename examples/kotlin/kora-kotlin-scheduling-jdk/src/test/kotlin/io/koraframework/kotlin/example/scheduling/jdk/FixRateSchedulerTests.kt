package io.koraframework.kotlin.example.scheduling.jdk

import org.awaitility.Awaitility
import org.junit.jupiter.api.Test
import io.koraframework.scheduling.jdk.FixedRateJob
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent
import java.time.Duration

@KoraAppTest(value = Application::class, components = [FixedRateJob::class])
class FixRateSchedulerTests {

    @TestComponent
    lateinit var scheduler: FixRateScheduler

    @Test
    fun scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until { scheduler.state > 3 }
    }
}
