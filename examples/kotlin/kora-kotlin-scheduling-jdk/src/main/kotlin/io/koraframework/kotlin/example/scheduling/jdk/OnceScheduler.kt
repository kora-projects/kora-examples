package io.koraframework.kotlin.example.scheduling.jdk

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkAtFixedRate
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkOnce
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkWithFixedDelay
import java.time.temporal.ChronoUnit

@Component
class OnceScheduler {
    var state = 0
        private set

    @ScheduleJdkOnce(delay = 50, unit = ChronoUnit.MILLIS)
    fun schedule() {
        state++
    }
}

