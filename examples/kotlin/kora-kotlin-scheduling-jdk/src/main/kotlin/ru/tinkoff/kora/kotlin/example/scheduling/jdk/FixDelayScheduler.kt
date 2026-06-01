package ru.tinkoff.kora.kotlin.example.scheduling.jdk

import ru.tinkoff.kora.common.Component
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleAtFixedRate
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleOnce
import ru.tinkoff.kora.scheduling.jdk.annotation.ScheduleWithFixedDelay
import java.time.temporal.ChronoUnit

@Component
class FixDelayScheduler {
    var state = 0
        private set

    @ScheduleWithFixedDelay(initialDelay = 50, delay = 50, unit = ChronoUnit.MILLIS)
    fun schedule() {
        state++
    }
}

