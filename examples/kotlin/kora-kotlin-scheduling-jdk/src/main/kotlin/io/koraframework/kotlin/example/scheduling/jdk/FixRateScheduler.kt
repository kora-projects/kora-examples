package io.koraframework.kotlin.example.scheduling.jdk

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.jdk.annotation.ScheduleAtFixedRate
import io.koraframework.scheduling.jdk.annotation.ScheduleOnce
import io.koraframework.scheduling.jdk.annotation.ScheduleWithFixedDelay
import java.time.temporal.ChronoUnit

@Component
class FixRateScheduler {
    var state = 0
        private set

    @ScheduleAtFixedRate(initialDelay = 50, period = 50, unit = ChronoUnit.MILLIS)
    fun schedule() {
        state++
    }
}

