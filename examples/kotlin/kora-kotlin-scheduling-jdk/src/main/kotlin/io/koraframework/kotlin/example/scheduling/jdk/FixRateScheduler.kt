package io.koraframework.kotlin.example.scheduling.jdk

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkAtFixedRate
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkOnce
import io.koraframework.scheduling.jdk.annotation.ScheduleJdkWithFixedDelay
import java.time.temporal.ChronoUnit

@Component
class FixRateScheduler {
    var state = 0
        private set

    @ScheduleJdkAtFixedRate(initialDelay = 50, period = 50, unit = ChronoUnit.MILLIS)
    fun schedule() {
        state++
    }
}

