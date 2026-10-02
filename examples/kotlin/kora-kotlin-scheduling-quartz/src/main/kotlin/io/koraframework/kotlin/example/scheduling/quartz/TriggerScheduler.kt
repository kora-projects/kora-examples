package io.koraframework.kotlin.example.scheduling.quartz

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithCron
import io.koraframework.scheduling.quartz.annotation.ScheduleQuartzWithTrigger

@Component
class TriggerScheduler {
    var state = 0
        private set

    @ScheduleQuartzWithTrigger(TriggerScheduler::class)
    fun schedule() {
        state++
    }
}
