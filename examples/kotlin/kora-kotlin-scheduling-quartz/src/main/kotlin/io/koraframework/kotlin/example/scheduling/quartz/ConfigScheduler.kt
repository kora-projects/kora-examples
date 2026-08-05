package io.koraframework.kotlin.example.scheduling.quartz

import io.koraframework.common.annotation.Component
import io.koraframework.common.annotation.Tag
import io.koraframework.scheduling.quartz.ScheduleWithCron
import io.koraframework.scheduling.quartz.ScheduleWithTrigger

@Component
class ConfigScheduler {
    var state = 0
        private set

    @ScheduleWithCron(config = "scheduling.jobs.quartz.cron")
    fun schedule() {
        state++
    }
}

