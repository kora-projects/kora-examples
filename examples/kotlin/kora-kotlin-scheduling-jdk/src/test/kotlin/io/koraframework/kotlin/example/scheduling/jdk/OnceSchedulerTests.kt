package io.koraframework.kotlin.example.scheduling.jdk

import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import io.koraframework.scheduling.jdk.RunOnceJob
import io.koraframework.test.extension.junit5.KoraAppTest
import io.koraframework.test.extension.junit5.TestComponent
import java.time.Duration

@KoraAppTest(value = Application::class, components = [RunOnceJob::class])
class OnceSchedulerTests {

    @TestComponent
    lateinit var scheduler: OnceScheduler

    @Test
    fun scheduled() {
        Awaitility.await().atMost(Duration.ofSeconds(3)).until { scheduler.state == 1 }
        assertThrows(ConditionTimeoutException::class.java) {
            Awaitility.await()
                .atMost(Duration.ofSeconds(1))
                .until { scheduler.state != 1 }
        }
    }
}
