package io.koraframework.kotlin.example.scheduling.jdk

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.jdk.annotation.ScheduleAtFixedRate
import io.koraframework.scheduling.jdk.annotation.ScheduleOnce
import io.koraframework.scheduling.jdk.annotation.ScheduleWithFixedDelay
import java.time.temporal.ChronoUnit

@Component
class ConfigScheduler {
    var state = 0
        private set

    @ScheduleAtFixedRate(config = "scheduling.jobs.fix-rate")
    fun schedule() {
        state++
    }
}

