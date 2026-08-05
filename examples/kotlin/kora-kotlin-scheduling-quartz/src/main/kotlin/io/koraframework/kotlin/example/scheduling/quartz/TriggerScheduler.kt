package io.koraframework.kotlin.example.scheduling.quartz

import io.koraframework.common.annotation.Component
import io.koraframework.scheduling.quartz.ScheduleWithCron
import io.koraframework.scheduling.quartz.ScheduleWithTrigger

@Component
class TriggerScheduler {
    var state = 0
        private set

    @ScheduleWithTrigger(TriggerScheduler::class)
    fun schedule() {
        state++
    }
}
